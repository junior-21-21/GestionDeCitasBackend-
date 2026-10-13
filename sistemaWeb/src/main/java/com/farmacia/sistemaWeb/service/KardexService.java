package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.KardexDTO;
import com.farmacia.sistemaWeb.entity.Kardex;
import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.entity.Usuario;
import com.farmacia.sistemaWeb.repository.KardexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KardexService {

    @Autowired
    private KardexRepository kardexRepository;

    /**
     * Registro simple de un movimiento sin lote. Las operaciones de stock usan {@link InventarioService},
     * que además guarda el lote, el signo y el documento de origen.
     */
    @Transactional
    public Kardex registrarMovimiento(Producto producto, int cantidad, Kardex.TipoOperacion tipoOperacion, String detalle, Usuario usuario) {
        Kardex kardex = new Kardex();
        kardex.setProducto(producto);
        kardex.setCantidad(Math.abs(cantidad));
        kardex.setMovimiento(tipoOperacion == Kardex.TipoOperacion.INGRESO ? "ENTRADA" : "SALIDA");
        kardex.setTipoOperacion(tipoOperacion);
        kardex.setDetalle(detalle);
        kardex.setSaldoResultante(producto.getStock());
        kardex.setUsuario(usuario);
        return kardexRepository.save(kardex);
    }

    @Transactional(readOnly = true)
    public List<KardexDTO> obtenerHistorialPorProducto(Long productoId) {
        return kardexRepository.findHistorialConDetalle(productoId).stream().map(KardexDTO::from).toList();
    }
}
