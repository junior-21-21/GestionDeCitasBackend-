package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.*;
import com.farmacia.sistemaWeb.repository.CompraRepository;
import com.farmacia.sistemaWeb.repository.LoteRepository;
import com.farmacia.sistemaWeb.repository.ProductoRepository;
import com.farmacia.sistemaWeb.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private CatalogoService catalogoService;

    @Transactional
    public Compra registrarCompra(Compra compraRequest) {
        if (compraRequest.getDetalles() == null || compraRequest.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La compra debe tener al menos un detalle");
        }

        // La compra siempre es nueva: un id enviado por el cliente sobrescribiría otra compra
        compraRequest.setId(null);
        compraRequest.setAnulada(null);
        compraRequest.setFechaAnulacion(null);
        compraRequest.setMotivoAnulacion(null);
        compraRequest.setFechaRegistro(null);
        // Quién registra la compra sale de la sesión, no de lo que envíe el cliente
        compraRequest.setUsuario(inventarioService.usuarioActual());
        if (compraRequest.getNumeroFactura() != null) {
            String factura = compraRequest.getNumeroFactura().trim();
            compraRequest.setNumeroFactura(factura.isEmpty() ? null : factura);
        }

        Proveedor proveedor = null;
        if (compraRequest.getProveedor() != null && compraRequest.getProveedor().getId() != null) {
            proveedor = proveedorRepository.findById(compraRequest.getProveedor().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        }
        compraRequest.setProveedor(proveedor);
        // La misma factura del mismo proveedor no se registra dos veces (doble envio = stock x2).
        if (proveedor != null && compraRequest.getNumeroFactura() != null
                && compraRepository.existeFacturaVigente(proveedor.getId(), compraRequest.getNumeroFactura())) {
            throw new IllegalArgumentException("La factura " + compraRequest.getNumeroFactura()
                    + " de este proveedor ya está registrada.");
        }

        List<Long> ids = new ArrayList<>();
        for (DetalleCompra detalle : compraRequest.getDetalles()) {
            if (detalle == null) {
                throw new IllegalArgumentException("Cada detalle debe indicar un producto");
            }
            Long productoId = catalogoService.productoIdDeLinea(detalle.getProducto(), detalle.getPresentacion());
            Producto ref = new Producto();
            ref.setId(productoId);
            detalle.setProducto(ref);
            ids.add(productoId);
        }
        // Bloqueo de los productos en orden fijo mientras se actualiza su stock y su costo
        Map<Long, Producto> productos = inventarioService.bloquearProductos(ids);

        BigDecimal total = BigDecimal.ZERO;
        List<LocalDate> vencimientos = new ArrayList<>();
        List<BigDecimal> costosBase = new ArrayList<>();
        for (DetalleCompra detalle : compraRequest.getDetalles()) {
            Producto producto = productos.get(detalle.getProducto().getId());
            if (!inventarioService.controlaStock(producto)) {
                throw new IllegalArgumentException("«" + producto.getNombre() + "» es un servicio: no se compra ni maneja stock");
            }
            if (!producto.isActivo()) {
                throw new IllegalArgumentException("El producto «" + producto.getNombre() + "» está desactivado");
            }
            Presentacion presentacion = catalogoService.resolver(producto, detalle.getPresentacion());
            if (presentacion.getId() != null && (!presentacion.isActivo() || !presentacion.isComprable())) {
                throw new IllegalArgumentException("La presentación «" + presentacion.getNombre() + "» de «"
                        + producto.getNombre() + "» no está habilitada para compras");
            }
            boolean porPresentacion = detalle.getPresentacion() != null && detalle.getPresentacion().getId() != null;
            // Con presentación: cantidad de presentaciones y costo de cada una; sin ella (clientes antiguos): unidades base
            int cantidadPres = porPresentacion && detalle.getCantidadPresentacion() != null
                    ? detalle.getCantidadPresentacion() : detalle.getCantidad();
            BigDecimal costoPres = porPresentacion && detalle.getCostoPresentacion() != null
                    ? detalle.getCostoPresentacion() : detalle.getPrecioUnitario();
            if (costoPres == null || costoPres.signum() < 0) {
                throw new IllegalArgumentException("El costo es obligatorio y no puede ser negativo");
            }
            if (cantidadPres <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }
            int factor = presentacion.getFactor() == null ? 1 : presentacion.getFactor();
            long enBase = (long) cantidadPres * factor;
            if (enBase > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Cantidad demasiado grande para «" + producto.getNombre() + "»");
            }
            detalle.setId(null);
            detalle.setLote(null);
            detalle.setProducto(producto);
            detalle.setPresentacion(presentacion.getId() != null ? presentacion : null);
            detalle.setFactor(factor);
            detalle.setCantidadPresentacion(cantidadPres);
            detalle.setCostoPresentacion(costoPres.setScale(2, RoundingMode.HALF_UP));
            detalle.setCantidad((int) enBase);
            vencimientos.add(inventarioService.validarIngreso(producto, detalle.getNumeroLote(), detalle.getFechaVencimiento()));

            BigDecimal subtotal = costoPres.multiply(BigDecimal.valueOf(cantidadPres)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal costoBase = costoPres.divide(BigDecimal.valueOf(factor), 6, RoundingMode.HALF_UP);
            costosBase.add(costoBase);
            detalle.setPrecioUnitario(costoBase.setScale(2, RoundingMode.HALF_UP));
            detalle.setSubtotal(subtotal);
            total = total.add(subtotal);
        }

        compraRequest.setTotal(total);
        Compra compra = compraRepository.save(compraRequest);
        String docId = String.valueOf(compra.getId());
        String detalleKardex = "Compra #" + compra.getId()
                + (compra.getNumeroFactura() != null ? " · Fact. " + compra.getNumeroFactura() : "")
                + (proveedor != null ? " · " + proveedor.getRazonSocial() : "");

        int i = 0;
        for (DetalleCompra detalle : compra.getDetalles()) {
            Producto producto = detalle.getProducto();
            LocalDate vencimiento = vencimientos.get(i);
            BigDecimal costoBase = costosBase.get(i);
            i++;

            // Costo promedio ponderado por unidad base: (stock * costo actual + cantidad * costo de compra) / stock nuevo
            actualizarCostoPromedio(producto, detalle.getCantidad(), costoBase);

            // Todo ingreso crea lote: si la factura no trae número de lote se genera uno
            String codigo = detalle.getNumeroLote() == null || detalle.getNumeroLote().isBlank()
                    ? "C" + compra.getId() + "-" + i
                    : detalle.getNumeroLote().trim();
            String texto = detalle.getPresentacion() != null
                    ? " · " + detalle.getCantidadPresentacion() + " " + detalle.getPresentacion().getNombre() : "";
            Lote lote = inventarioService.ingresarNuevoLote(producto, detalle.getCantidad(), codigo, vencimiento,
                    Kardex.TipoOperacion.INGRESO, detalleKardex + texto, "COMPRA", docId);
            detalle.setLote(lote);
            detalle.setNumeroLote(lote.getCodigoLote());
            detalle.setFechaVencimiento(lote.isSinVencimiento() ? null : vencimiento);
        }
        return compra;
    }

    /** Costo unitario actual con 6 decimales (o el precio de compra si aún no tiene). */
    private BigDecimal costoActual(Producto producto) {
        return producto.getCostoUnitario() != null ? producto.getCostoUnitario() : producto.getPrecioCompra();
    }

    private void fijarCosto(Producto producto, BigDecimal costo) {
        producto.setCostoUnitario(costo.setScale(6, RoundingMode.HALF_UP));
        producto.setPrecioCompra(costo.setScale(2, RoundingMode.HALF_UP));
    }

    private void actualizarCostoPromedio(Producto producto, int cantidad, BigDecimal costoCompra) {
        int stockPrevio = producto.getStock() == null ? 0 : Math.max(0, producto.getStock());
        BigDecimal costoActual = costoActual(producto);
        BigDecimal nuevo;
        if (stockPrevio == 0 || costoActual == null) {
            nuevo = costoCompra;
        } else {
            nuevo = costoActual.multiply(BigDecimal.valueOf(stockPrevio))
                    .add(costoCompra.multiply(BigDecimal.valueOf(cantidad)))
                    .divide(BigDecimal.valueOf((long) stockPrevio + cantidad), 6, RoundingMode.HALF_UP);
        }
        fijarCosto(producto, nuevo);
    }

    /**
     * Anula una compra: retira de sus lotes las unidades que ingresaron (movimiento ANULACION en el kardex),
     * revierte el costo promedio y marca la compra como anulada. Falla si parte de esos lotes ya se usó.
     */
    @Transactional
    public Compra anularCompra(Long id, String motivo) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));
        if (compra.estaAnulada()) {
            throw new IllegalArgumentException("La compra #" + id + " ya fue anulada");
        }
        List<Long> ids = new ArrayList<>();
        compra.getDetalles().forEach(d -> ids.add(d.getProducto().getId()));
        inventarioService.bloquearProductos(ids);

        // 1) Validar todo antes de mover stock
        List<Lote> lotes = new ArrayList<>();
        for (DetalleCompra d : compra.getDetalles()) {
            Lote lote = d.getLote();
            if (lote == null) {
                // Compras anteriores: buscar el lote por código, producto y cantidad
                lote = loteRepository.findByProductoId(d.getProducto().getId()).stream()
                        .filter(l -> d.getNumeroLote() != null && d.getNumeroLote().equals(l.getCodigoLote())
                                && l.getCantidadInicial() != null && l.getCantidadInicial() == d.getCantidad()
                                && !lotes.contains(l))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("La compra #" + id
                                + " es anterior al control por lotes y no se puede anular automáticamente; registre un ajuste de salida"));
            }
            int stockLote = lote.getStockActual() == null ? 0 : lote.getStockActual();
            if (stockLote < d.getCantidad()) {
                throw new IllegalArgumentException("No se puede anular la compra #" + id + ": del lote " + lote.getCodigoLote()
                        + " de «" + d.getProducto().getNombre() + "» ya se usaron " + (d.getCantidad() - stockLote)
                        + " de " + d.getCantidad() + " unidades");
            }
            lotes.add(lote);
        }

        // 2) Retirar las unidades y revertir el costo promedio
        String motivoLimpio = motivo == null || motivo.isBlank() ? null : motivo.trim();
        String detalle = "Anulación de compra #" + id + (motivoLimpio != null ? ": " + motivoLimpio : "");
        int i = 0;
        for (DetalleCompra d : compra.getDetalles()) {
            Lote lote = lotes.get(i++);
            Producto producto = d.getProducto();
            int stockAntes = producto.getStock() == null ? 0 : producto.getStock();
            inventarioService.descontarDeLote(lote.getId(), d.getCantidad(), true, Kardex.TipoOperacion.ANULACION,
                    detalle, "COMPRA", String.valueOf(id));
            int stockDespues = producto.getStock() == null ? 0 : producto.getStock();
            // El subtotal es el costo exacto de lo que ingresó (precioUnitario está redondeado a 2 decimales)
            BigDecimal costoIngreso = d.getSubtotal() != null ? d.getSubtotal()
                    : (d.getPrecioUnitario() != null ? d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())) : null);
            if (stockDespues > 0 && costoActual(producto) != null && costoIngreso != null) {
                BigDecimal revertido = costoActual(producto).multiply(BigDecimal.valueOf(stockAntes))
                        .subtract(costoIngreso)
                        .divide(BigDecimal.valueOf(stockDespues), 6, RoundingMode.HALF_UP);
                if (revertido.signum() > 0) {
                    fijarCosto(producto, revertido);
                }
            }
            productoRepository.save(producto);
        }

        compra.setAnulada(true);
        compra.setFechaAnulacion(LocalDateTime.now());
        compra.setMotivoAnulacion(motivoLimpio != null && motivoLimpio.length() > 255 ? motivoLimpio.substring(0, 255) : motivoLimpio);
        return compraRepository.save(compra);
    }

    public List<Compra> listarCompras() {
        return compraRepository.findAllRecientes();
    }
}
