package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.DetalleVenta;
import com.farmacia.sistemaWeb.entity.Presentacion;
import com.farmacia.sistemaWeb.entity.UnidadBase;
import com.farmacia.sistemaWeb.entity.Kardex;
import com.farmacia.sistemaWeb.entity.Lote;
import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.entity.Venta;
import com.farmacia.sistemaWeb.repository.KardexRepository;
import com.farmacia.sistemaWeb.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private KardexRepository kardexRepository;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private CatalogoService catalogoService;

    @Transactional
    public Venta registrarVentaDirecta(Venta ventaRequest) {
        if (ventaRequest.getDetalles() == null || ventaRequest.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un producto");
        }

        // La venta siempre es nueva: si el cliente enviaba un id existente, save() sobrescribía esa venta
        ventaRequest.setId(null);
        ventaRequest.setFecha(null);
        ventaRequest.setAnulada(null);
        ventaRequest.setFechaAnulacion(null);
        ventaRequest.setMotivoAnulacion(null);
        if (ventaRequest.getClienteDni() != null && !ventaRequest.getClienteDni().isBlank()
                && !ventaRequest.getClienteDni().matches("^(\\d{8}|\\d{11})$")) {
            throw new IllegalArgumentException("El documento del cliente debe ser un DNI de 8 dígitos o un RUC de 11 dígitos");
        }

        List<Long> ids = new ArrayList<>();
        for (DetalleVenta detalle : ventaRequest.getDetalles()) {
            if (detalle == null) {
                throw new IllegalArgumentException("Cada detalle debe indicar un producto");
            }
            Long productoId = catalogoService.productoIdDeLinea(detalle.getProducto(), detalle.getPresentacion());
            Producto ref = new Producto();
            ref.setId(productoId);
            detalle.setProducto(ref);
            ids.add(productoId);
        }
        // Se bloquean los productos (en orden fijo) antes de leer su stock: dos ventas simultáneas
        // de la última unidad se atienden una detrás de otra y la segunda recibe "stock insuficiente".
        Map<Long, Producto> productos = inventarioService.bloquearProductos(ids);

        double total = 0.0;
        List<String> textos = new ArrayList<>();
        for (DetalleVenta detalle : ventaRequest.getDetalles()) {
            Producto producto = productos.get(detalle.getProducto().getId());
            if (!producto.isActivo()) {
                throw new IllegalArgumentException("El producto «" + producto.getNombre() + "» está desactivado");
            }
            Presentacion presentacion = catalogoService.resolver(producto, detalle.getPresentacion());
            if (presentacion.getId() != null && (!presentacion.isActivo() || !presentacion.isVendible())) {
                throw new IllegalArgumentException("La presentación «" + presentacion.getNombre() + "» de «"
                        + producto.getNombre() + "» no está habilitada para la venta");
            }

            // Cantidad en la presentación: cantidadPresentacion (admite decimales) o cantidad
            BigDecimal cantidadPres = detalle.getCantidadPresentacion() != null ? detalle.getCantidadPresentacion()
                    : (detalle.getCantidad() != null ? BigDecimal.valueOf(detalle.getCantidad()) : null);
            if (cantidadPres == null || cantidadPres.signum() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }
            cantidadPres = cantidadPres.stripTrailingZeros();
            UnidadBase unidad = producto.getUnidadBase() != null ? producto.getUnidadBase() : UnidadBase.UNIDAD;
            boolean conDecimales = cantidadPres.scale() > 0;
            if (conDecimales && !unidad.isFraccionable()) {
                throw new IllegalArgumentException("«" + producto.getNombre() + " · " + presentacion.getNombre()
                        + "» se vende en cantidades enteras (" + cantidadPres.toPlainString() + " no es válido)");
            }
            if (cantidadPres.scale() > 3) {
                throw new IllegalArgumentException("La cantidad admite como máximo 3 decimales");
            }
            int factor = presentacion.getFactor() == null ? 1 : presentacion.getFactor();
            BigDecimal enBase = cantidadPres.multiply(BigDecimal.valueOf(factor)).setScale(0, RoundingMode.HALF_UP);
            if (enBase.signum() <= 0 || enBase.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
                throw new IllegalArgumentException("Cantidad no válida para «" + producto.getNombre() + "»");
            }

            detalle.setId(null);
            // Re-asignar el producto real para persistir correctamente
            detalle.setProducto(producto);
            detalle.setPresentacion(presentacion.getId() != null ? presentacion : null);
            detalle.setFactor(factor);
            detalle.setCantidadPresentacion(cantidadPres.setScale(3, RoundingMode.HALF_UP));
            detalle.setCantidad(enBase.intValue());

            // Precio de la presentación (no el que envíe el cliente)
            BigDecimal precio = presentacion.getPrecioVenta() != null ? presentacion.getPrecioVenta() : producto.getPrecioVenta();
            BigDecimal subtotal = precio.multiply(cantidadPres).setScale(2, RoundingMode.HALF_UP);
            detalle.setPrecioUnitario(precio.doubleValue());
            detalle.setSubtotal(subtotal.doubleValue());
            total += subtotal.doubleValue();
            textos.add(cantidadPres.toPlainString() + " " + presentacion.getNombre());
        }

        ventaRequest.setTotal(BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP).doubleValue());
        Venta ventaGuardada = ventaRepository.save(ventaRequest);
        String docId = String.valueOf(ventaGuardada.getId());

        // Descuento por lotes (FEFO, sin vencidos) en unidades base. Los servicios no descuentan stock.
        // Si algún producto no alcanza, se lanza la excepción y toda la venta se revierte.
        int i = 0;
        for (DetalleVenta detalle : ventaGuardada.getDetalles()) {
            String texto = textos.get(i++);
            inventarioService.descontarFefo(detalle.getProducto(), detalle.getCantidad(),
                    Kardex.TipoOperacion.VENTA, "Venta POS #" + ventaGuardada.getId() + " · " + texto, "VENTA", docId);
        }

        return ventaGuardada;
    }

    /**
     * Anula una venta: devuelve las unidades a los mismos lotes de los que salieron (movimiento ANULACION)
     * y marca la venta como anulada. Una venta no se puede anular dos veces.
     */
    @Transactional
    public Venta anularVenta(Long id, String motivo) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        if (venta.estaAnulada()) {
            throw new IllegalArgumentException("La venta #" + id + " ya fue anulada");
        }
        List<Long> ids = new ArrayList<>();
        venta.getDetalles().forEach(d -> ids.add(d.getProducto().getId()));
        inventarioService.bloquearProductos(ids);

        String motivoLimpio = motivo == null || motivo.isBlank() ? null : motivo.trim();
        String detalle = "Anulación de venta #" + id + (motivoLimpio != null ? ": " + motivoLimpio : "");
        String docId = String.valueOf(id);

        // Salidas registradas para esta venta, por lote
        Map<Long, Integer> devueltoPorProducto = new LinkedHashMap<>();
        for (Kardex k : kardexRepository.findByDocumento("VENTA", docId)) {
            if (k.getTipoOperacion() != Kardex.TipoOperacion.VENTA || k.esEntrada() || k.getLote() == null) continue;
            inventarioService.ingresarEnLote(k.getLote(), Math.abs(k.getCantidad()), Kardex.TipoOperacion.ANULACION,
                    detalle, "VENTA", docId);
            devueltoPorProducto.merge(k.getProducto().getId(), Math.abs(k.getCantidad()), Integer::sum);
        }

        // Ventas anteriores al control por lotes (sin lote en el kardex): se devuelven a un lote de devolución
        Map<Long, Integer> vendidoPorProducto = new LinkedHashMap<>();
        Map<Long, Producto> productoPorId = new LinkedHashMap<>();
        for (DetalleVenta d : venta.getDetalles()) {
            if (!inventarioService.controlaStock(d.getProducto())) continue;
            vendidoPorProducto.merge(d.getProducto().getId(), d.getCantidad(), Integer::sum);
            productoPorId.put(d.getProducto().getId(), d.getProducto());
        }
        for (Map.Entry<Long, Integer> e : vendidoPorProducto.entrySet()) {
            int pendiente = e.getValue() - devueltoPorProducto.getOrDefault(e.getKey(), 0);
            if (pendiente > 0) {
                Producto p = productoPorId.get(e.getKey());
                inventarioService.ingresarNuevoLote(p, pendiente, "DEV-V" + id, Lote.SIN_VENCIMIENTO,
                        Kardex.TipoOperacion.ANULACION, detalle, "VENTA", docId);
            }
        }

        venta.setAnulada(true);
        venta.setFechaAnulacion(LocalDateTime.now());
        venta.setMotivoAnulacion(motivoLimpio != null && motivoLimpio.length() > 255 ? motivoLimpio.substring(0, 255) : motivoLimpio);
        return ventaRepository.save(venta);
    }

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    public Venta obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
    }
}
