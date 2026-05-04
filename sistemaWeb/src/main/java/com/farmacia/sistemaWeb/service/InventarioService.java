package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.LoteDTO;
import com.farmacia.sistemaWeb.entity.Lote;
import com.farmacia.sistemaWeb.entity.MovimientoInventario;
import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.entity.TipoMovimiento;
import com.farmacia.sistemaWeb.entity.Usuario;
import com.farmacia.sistemaWeb.repository.LoteRepository;
import com.farmacia.sistemaWeb.repository.MovimientoInventarioRepository;
import com.farmacia.sistemaWeb.repository.ProductoRepository;
import com.farmacia.sistemaWeb.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventarioService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public void registrarEntrada(Long usuarioId, String productoCodigoBarras, LoteDTO loteDTO) {
        Producto producto = productoRepository.findById(productoCodigoBarras)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Lote lote = new Lote();
        lote.setProducto(producto);
        lote.setNumeroLote(loteDTO.getNumeroLote());
        lote.setFechaVencimiento(loteDTO.getFechaVencimiento());
        lote.setStockInicial(loteDTO.getStockInicial());
        lote.setStockActual(loteDTO.getStockInicial());
        lote.setCostoUnitario(loteDTO.getCostoUnitario());
        lote.setFechaIngreso(LocalDateTime.now());

        lote = loteRepository.save(lote);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setLote(lote);
        movimiento.setUsuario(usuario);
        movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
        movimiento.setCantidad(loteDTO.getStockInicial());
        movimiento.setFechaHora(LocalDateTime.now());
        movimientoRepository.save(movimiento);

        int oldStock = producto.getStockActual();
        double oldCosto = producto.getCostoPromedio() != null ? producto.getCostoPromedio() : 0.0;

        producto.setStockActual(oldStock + loteDTO.getStockInicial());

        if (producto.getStockActual() > 0) {
            double newCosto = ((oldStock * oldCosto) + (loteDTO.getStockInicial() * loteDTO.getCostoUnitario()))
                    / producto.getStockActual();
            producto.setCostoPromedio(newCosto);
        }

        productoRepository.save(producto);
    }

    public List<Lote> obtenerLotesDisponibles(String codigoBarras) {
        return loteRepository
                .findByProductoCodigoBarrasAndStockActualGreaterThanOrderByFechaVencimientoAsc(codigoBarras, 0);
    }

    public List<MovimientoInventario> obtenerHistorialMovimientos(String codigoBarras) {
        return movimientoRepository.findByProductoCodigoBarrasOrderByFechaHoraDesc(codigoBarras);
    }

    public List<MovimientoInventario> obtenerLibroEstupefacientes() {
        return movimientoRepository.findByProductoIsControladoTrueOrderByFechaHoraDesc();
    }

    @Transactional
    public void ajustarInventario(Long usuarioId, String productoCodigoBarras, Long loteId, int cantidadAjuste,
            String motivo, TipoMovimiento tipo) {
        if (tipo != TipoMovimiento.AJUSTE_MERMA && tipo != TipoMovimiento.AJUSTE_CADUCIDAD) {
            throw new RuntimeException("Tipo de movimiento no válido para ajuste manual.");
        }

        Producto producto = productoRepository.findById(productoCodigoBarras)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Lote lote = null;
        if (loteId != null) {
            lote = loteRepository.findById(loteId)
                    .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
            if (lote.getStockActual() < Math.abs(cantidadAjuste) && cantidadAjuste < 0) {
                throw new RuntimeException("No hay suficiente stock en el lote para este ajuste.");
            }
            lote.setStockActual(lote.getStockActual() + cantidadAjuste);
            loteRepository.save(lote);
        }

        if (producto.getStockActual() < Math.abs(cantidadAjuste) && cantidadAjuste < 0) {
            throw new RuntimeException("No hay suficiente stock total para este ajuste.");
        }

        producto.setStockActual(producto.getStockActual() + cantidadAjuste);
        productoRepository.save(producto);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setLote(lote);
        movimiento.setUsuario(usuario);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(cantidadAjuste);
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setMotivoAjuste(motivo);
        movimientoRepository.save(movimiento);
    }
}
