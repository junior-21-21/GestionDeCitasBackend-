package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.DetalleVenta;
import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.entity.Venta;
import com.farmacia.sistemaWeb.repository.ProductoRepository;
import com.farmacia.sistemaWeb.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private LoteService loteService;

    @Autowired
    private KardexService kardexService;

    @Transactional
    public Venta registrarVentaDirecta(Venta ventaRequest) {
        if (ventaRequest.getDetalles() == null || ventaRequest.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un producto");
        }

        double total = 0.0;
        
        // Procesar cada detalle de la venta
        for (DetalleVenta detalle : ventaRequest.getDetalles()) {
            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            
            // Re-asignar el producto real para persistir correctamente
            detalle.setProducto(producto);
            
            if (detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }

            // Descontar stock usando Lotes (FIFO)
            loteService.descontarStock(producto.getId(), detalle.getCantidad());

            // Calcular subtotal real (por si manipularon precios en frontend)
            detalle.setPrecioUnitario(producto.getPrecioVenta().doubleValue());
            double subtotal = detalle.getCantidad() * detalle.getPrecioUnitario();
            detalle.setSubtotal(subtotal);
            
            total += subtotal;
        }

        ventaRequest.setTotal(total);
        Venta ventaGuardada = ventaRepository.save(ventaRequest);

        // Registrar kardex DESPUÉS de guardar la venta para poder obtener el ID si lo quisieramos referenciar
        for (DetalleVenta detalle : ventaGuardada.getDetalles()) {
             kardexService.registrarMovimiento(
                     detalle.getProducto(),
                     detalle.getCantidad(),
                     com.farmacia.sistemaWeb.entity.Kardex.TipoOperacion.VENTA,
                     "Venta POS #" + ventaGuardada.getId(),
                     null // ventaRequest no tiene getUsuario()
             );
        }

        return ventaGuardada;
    }

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }
}
