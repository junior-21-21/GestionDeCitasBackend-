package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.Kardex;
import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.repository.CompraRepository;
import com.farmacia.sistemaWeb.repository.KardexRepository;
import com.farmacia.sistemaWeb.repository.LoteRepository;
import com.farmacia.sistemaWeb.repository.ProductoRepository;
import com.farmacia.sistemaWeb.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private KardexRepository kardexRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private CatalogoService catalogoService;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    private void validar(Producto producto) {
        if (producto.getCodigo() == null || producto.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código del producto es obligatorio");
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta().signum() < 0) {
            throw new IllegalArgumentException("El precio de venta es obligatorio y no puede ser negativo");
        }
        if (producto.getPrecioCompra() != null && producto.getPrecioCompra().signum() < 0) {
            throw new IllegalArgumentException("El precio de compra no puede ser negativo");
        }
        if (producto.getStockMinimo() != null && producto.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
        if (producto.getClase() == null && producto.getTipo() == null) {
            throw new IllegalArgumentException("La clase del producto es obligatoria");
        }
        if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        if (producto.getPrecioCompra() == null) producto.setPrecioCompra(BigDecimal.ZERO);
        producto.setPrecioVenta(producto.getPrecioVenta().setScale(2, java.math.RoundingMode.HALF_UP));
        producto.setPrecioCompra(producto.getPrecioCompra().setScale(2, java.math.RoundingMode.HALF_UP));
        if (producto.getStockMinimo() == null) producto.setStockMinimo(0);
        // Código único también al editar (antes solo se comprobaba al crear)
        productoRepository.findByCodigo(producto.getCodigo())
                .filter(otro -> !otro.getId().equals(producto.getId()))
                .ifPresent(otro -> {
                    throw new IllegalArgumentException("Ya existe un producto con el código " + producto.getCodigo());
                });
    }

    /**
     * Alta de producto. El stock no se escribe a mano: si se indica un stock inicial se registra
     * como un lote y un movimiento de ingreso en el kardex.
     */
    @Transactional
    public Producto crear(Producto producto) {
        producto.setId(null);
        validar(producto);
        Integer inicial = producto.getStockInicial();
        // Compatibilidad: clientes antiguos enviaban el stock inicial en "stock"
        if (inicial == null && producto.getStock() != null && producto.getStock() > 0) {
            inicial = producto.getStock();
        }
        if (inicial != null && inicial < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        }
        String loteInicial = producto.getLoteInicial();
        java.time.LocalDate vencimiento = producto.getVencimientoInicial();
        java.util.List<com.farmacia.sistemaWeb.entity.Presentacion> presentaciones = producto.getPresentaciones();
        catalogoService.aplicarClase(producto, null);
        producto.setPresentaciones(new java.util.ArrayList<>());
        producto.setStock(0);
        producto.setActivo(true);
        producto.setCostoUnitario(producto.getPrecioCompra().setScale(6, java.math.RoundingMode.HALF_UP));
        if (inicial != null && inicial > 0 && inventarioService.controlaStock(producto)) {
            // Se valida antes de guardar para no dejar el producto a medias
            inventarioService.validarIngreso(producto, loteInicial, vencimiento);
        }
        Producto guardado = productoRepository.save(producto);
        catalogoService.sincronizar(guardado, presentaciones);

        if (inicial != null && inicial > 0 && inventarioService.controlaStock(guardado)) {
            Producto bloqueado = inventarioService.bloquearProducto(guardado.getId());
            java.time.LocalDate venc = inventarioService.validarIngreso(bloqueado, loteInicial, vencimiento);
            String codigo = loteInicial == null || loteInicial.isBlank() ? "INICIAL-" + bloqueado.getCodigo() : loteInicial.trim();
            inventarioService.ingresarNuevoLote(bloqueado, inicial, codigo, venc, Kardex.TipoOperacion.INGRESO,
                    "Stock inicial al registrar el producto", "INICIAL", String.valueOf(bloqueado.getId()));
        }
        return guardado;
    }

    /** Edición: el stock no cambia por aquí (solo por compras, ventas, consumos y ajustes). */
    @Transactional
    public Producto actualizar(Long id, Producto producto) {
        Producto actual = inventarioService.bloquearProducto(id);
        producto.setId(id);
        validar(producto);
        catalogoService.aplicarClase(producto, actual);
        if (actual.getUnidadBase() != null && producto.getUnidadBase() != actual.getUnidadBase()
                && actual.getStock() != null && actual.getStock() > 0) {
            throw new IllegalArgumentException("No se puede cambiar la unidad base de «" + actual.getNombre()
                    + "» mientras tenga stock (" + actual.getStock() + " " + inventarioService.unidades(actual)
                    + "): el stock está contado en esa unidad");
        }
        if (actual.getTipo() != producto.getTipo()
                && (producto.getTipo() == Producto.TipoProducto.SERVICIO || actual.getTipo() == Producto.TipoProducto.SERVICIO)
                && actual.getStock() != null && actual.getStock() > 0) {
            throw new IllegalArgumentException("No se puede cambiar a/desde SERVICIO un producto con stock; primero ajuste su stock a 0");
        }
        actual.setCodigo(producto.getCodigo());
        actual.setNombre(producto.getNombre());
        actual.setDescripcion(producto.getDescripcion());
        if (actual.getPrecioCompra() == null || actual.getPrecioCompra().compareTo(producto.getPrecioCompra()) != 0
                || actual.getCostoUnitario() == null) {
            // Costo corregido a mano: pasa a ser el costo unitario exacto
            actual.setCostoUnitario(producto.getPrecioCompra().setScale(6, java.math.RoundingMode.HALF_UP));
        }
        actual.setPrecioCompra(producto.getPrecioCompra());
        actual.setPrecioVenta(producto.getPrecioVenta());
        actual.setStockMinimo(producto.getStockMinimo());
        actual.setTipo(producto.getTipo());
        actual.setClase(producto.getClase());
        actual.setLineaNegocio(producto.getLineaNegocio());
        actual.setRequiereReceta(producto.getRequiereReceta());
        actual.setControlado(producto.getControlado());
        actual.setRefrigerado(producto.getRefrigerado());
        actual.setRequiereLote(producto.getRequiereLote());
        actual.setUnidadBase(producto.getUnidadBase());
        actual.setCategoria(producto.getCategoria());
        actual.setActivo(producto.isActivo());
        Producto guardado = productoRepository.save(actual);
        catalogoService.sincronizar(guardado, producto.getPresentaciones());
        return guardado;
    }

    /** @deprecated usar {@link #crear} o {@link #actualizar}. */
    @Deprecated
    public Producto guardar(Producto producto) {
        return producto.getId() == null ? crear(producto) : actualizar(producto.getId(), producto);
    }

    /**
     * Un producto con historial (lotes, movimientos, ventas o compras) no se borra: se desactiva.
     * @return true si se desactivó, false si se eliminó físicamente
     */
    @Transactional
    public boolean eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        boolean tieneHistorial = loteRepository.existsByProductoId(id)
                || kardexRepository.existsByProductoId(id)
                || ventaRepository.existsDetalleConProducto(id)
                || compraRepository.existsDetalleConProducto(id);
        if (tieneHistorial) {
            producto.setActivo(false);
            productoRepository.save(producto);
            return true;
        }
        productoRepository.delete(producto);
        return false;
    }
}
