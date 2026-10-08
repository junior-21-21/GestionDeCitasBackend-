package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.*;
import com.farmacia.sistemaWeb.repository.CompraRepository;
import com.farmacia.sistemaWeb.repository.LoteRepository;
import com.farmacia.sistemaWeb.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private KardexService kardexService;

    @Transactional
    public Compra registrarCompra(Compra compraRequest) {
        if (compraRequest.getDetalles() == null || compraRequest.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La compra debe tener al menos un detalle");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleCompra detalle : compraRequest.getDetalles()) {
            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + detalle.getProducto().getId()));
            
            detalle.setProducto(producto);

            if (detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }

            // Calcular subtotal
            BigDecimal subtotal = detalle.getPrecioUnitario().multiply(new BigDecimal(detalle.getCantidad()));
            detalle.setSubtotal(subtotal);
            total = total.add(subtotal);

            // Crear el nuevo lote para el producto
            if (detalle.getNumeroLote() != null && !detalle.getNumeroLote().isEmpty() && detalle.getFechaVencimiento() != null) {
                Lote nuevoLote = new Lote();
                nuevoLote.setProducto(producto);
                nuevoLote.setCodigoLote(detalle.getNumeroLote());
                nuevoLote.setFechaVencimiento(detalle.getFechaVencimiento());
                nuevoLote.setCantidadInicial(detalle.getCantidad());
                nuevoLote.setStockActual(detalle.getCantidad());
                // nuevoLote.setActivo(true);
                loteRepository.save(nuevoLote);
            }

            // Actualizar stock global del producto
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);

            // Registrar en el Kardex
            kardexService.registrarMovimiento(
                    producto,
                    detalle.getCantidad(),
                    Kardex.TipoOperacion.INGRESO,
                    "Compra Fra. " + compraRequest.getNumeroFactura(),
                    compraRequest.getUsuario()
            );
        }

        compraRequest.setTotal(total);
        return compraRepository.save(compraRequest);
    }

    public List<Compra> listarCompras() {
        return compraRepository.findAll();
    }
}
