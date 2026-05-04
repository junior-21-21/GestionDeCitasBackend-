package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.VentaDTO;
import com.farmacia.sistemaWeb.dto.VentaResponseDTO;
import com.farmacia.sistemaWeb.dto.DetalleVentaDTO;
import com.farmacia.sistemaWeb.entity.*;
import com.farmacia.sistemaWeb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProductoRepository productoRepository;

    private String generarSerie(String tipo) {
        if ("FACTURA".equalsIgnoreCase(tipo))
            return "F001";
        return "B001";
    }

    private String generarCorrelativoStr(String tipoComprobante) {
        String serie = generarSerie(tipoComprobante);
        // Simple strategy: count total ventas of this type
        long count = ventaRepository.count(); // Ideally: countBySerie(serie) if we had that custom query
        return String.format("%08d", count + 1);
    }

    private String generarCodigoVenta(LocalDate fecha) {
        String fechaStr = fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = ventaRepository.countByFecha(fecha);
        return String.format("VEN-%s-%03d", fechaStr, count + 1);
    }

    public Venta registrarVenta(VentaDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteDni())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        LocalDate hoy = LocalDate.now();
        String codigoVenta = generarCodigoVenta(hoy);

        String tipoComprobante = dto.getTipoComprobante() != null ? dto.getTipoComprobante().toUpperCase() : "BOLETA";
        boolean isFactura = "FACTURA".equals(tipoComprobante);

        // Validation for Factura
        if (isFactura) {
            if (cliente.getRazonSocial() == null || cliente.getRazonSocial().isEmpty() ||
                    cliente.getDni() == null || cliente.getDni().length() != 11) {
                throw new RuntimeException(
                        "Para emitir FACTURA, el cliente debe tener RUC (11 dígitos) y Razón Social");
            }
        }

        String serie = generarSerie(tipoComprobante);
        String correlativo = generarCorrelativoStr(tipoComprobante);

        Venta venta = new Venta();
        venta.setCodigoVenta(serie + "-" + correlativo); // Legacy support
        venta.setTipoComprobante(tipoComprobante);
        venta.setSerie(serie);
        venta.setCorrelativo(correlativo);
        venta.setMetodoPago(dto.getMetodoPago() != null ? dto.getMetodoPago() : "EFECTIVO");
        venta.setCliente(cliente);
        venta.setFecha(hoy);
        venta.setRecetaMedica(dto.getRecetaMedica());

        // Validar medicamentos controlados
        boolean requiereReceta = false;
        for (DetalleVentaDTO d : dto.getDetalles()) {
            Producto p = productoRepository.findById(d.getCodigoBarras())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + d.getCodigoBarras()));
            if (p.getIsControlado()) {
                requiereReceta = true;
                break;
            }
        }

        if (requiereReceta && (dto.getRecetaMedica() == null || dto.getRecetaMedica().trim().isEmpty())) {
            throw new RuntimeException(
                    "La venta incluye medicamentos controlados. Se requiere el Nro de Receta Médica.");
        }

        List<DetalleVenta> detalles = new ArrayList<>();
        double total = 0;
        int lineaNum = 1;

        for (DetalleVentaDTO d : dto.getDetalles()) {
            Producto producto = productoRepository.findById(d.getCodigoBarras())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Logic for FIFO and stock deduction...
            // Note: Simplification for now, we just deduct from stockActual as before,
            // but ideally we'd deduct from Lote entities ordered by expiration date (FIFO)
            if (producto.getTipoInventario() != TipoInventario.SERVICIO) {
                if (producto.getStockActual() < d.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
                }
                producto.setStockActual(producto.getStockActual() - d.getCantidad());
            }

            double precioItem = producto.getPrecioVenta() * d.getCantidad();

            DetalleVenta det = new DetalleVenta();
            det.setCodigoDetalle(String.format("DV-%s-%02d", codigoVenta.substring(4), lineaNum++));
            det.setCantidad(d.getCantidad());
            det.setPrecio(precioItem);
            det.setProducto(producto);
            det.setVenta(venta);

            detalles.add(det);
            total += precioItem;
        }

        double subtotal = 0;
        double igv = 0;

        // Si es factura (o en general en Perú, boleta también incluye IGV por dentro)
        // Cálculo del IGV (18% de Perú)
        subtotal = total / 1.18;
        igv = total - subtotal;

        // Redondear a 2 decimales
        subtotal = Math.round(subtotal * 100.0) / 100.0;
        igv = Math.round(igv * 100.0) / 100.0;
        total = Math.round(total * 100.0) / 100.0;

        venta.setSubtotal(subtotal);
        venta.setIgv(igv);
        venta.setTotal(total);
        venta.setDetalles(detalles);

        return ventaRepository.save(venta);
    }

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    public Venta obtenerPorCodigo(String codigoVenta) {
        return ventaRepository.findByCodigoVenta(codigoVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con código: " + codigoVenta));
    }

    public VentaResponseDTO mapToResponseDTO(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(venta.getId());
        dto.setCodigoVenta(venta.getCodigoVenta());
        dto.setTipoComprobante(venta.getTipoComprobante());
        dto.setSerie(venta.getSerie());
        dto.setCorrelativo(venta.getCorrelativo());
        dto.setSubtotal(venta.getSubtotal());
        dto.setIgv(venta.getIgv());
        dto.setDescuento(venta.getDescuento());
        dto.setTotal(venta.getTotal());
        dto.setMetodoPago(venta.getMetodoPago());
        dto.setFecha(venta.getFecha());
        dto.setRecetaMedica(venta.getRecetaMedica());

        if (venta.getCliente() != null) {
            VentaResponseDTO.ClienteInfo ci = new VentaResponseDTO.ClienteInfo();
            ci.setDni(venta.getCliente().getDni());
            ci.setNombres(venta.getCliente().getNombres());
            ci.setApellidos(venta.getCliente().getApellidos());
            ci.setRazonSocial(venta.getCliente().getRazonSocial());
            ci.setRuc("RUC".equals(venta.getCliente().getTipoDocumento()) ? venta.getCliente().getDni() : null);
            dto.setCliente(ci);
        }

        if (venta.getDetalles() != null) {
            List<VentaResponseDTO.DetalleVentaInfo> detallesInfo = venta.getDetalles().stream().map(d -> {
                VentaResponseDTO.DetalleVentaInfo di = new VentaResponseDTO.DetalleVentaInfo();
                di.setCodigoDetalle(d.getCodigoDetalle());
                di.setCantidad(d.getCantidad());
                di.setPrecio(d.getPrecio());
                if (d.getProducto() != null) {
                    di.setProductoCodigoBarras(d.getProducto().getCodigoBarras());
                    di.setProductoNombre(d.getProducto().getNombre());
                }
                return di;
            }).collect(java.util.stream.Collectors.toList());
            dto.setDetalles(detallesInfo);
        }

        return dto;
    }
}
