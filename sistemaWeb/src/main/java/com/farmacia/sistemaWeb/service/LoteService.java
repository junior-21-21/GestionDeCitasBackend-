package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.Kardex;
import com.farmacia.sistemaWeb.entity.Lote;
import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.repository.LoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Compatibilidad: la lógica real de stock está en {@link InventarioService}
 * (FEFO sin vencidos, bloqueo del producto, kardex con lote y usuario).
 */
@Service
public class LoteService {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private LoteRepository loteRepository;

    /** Descuenta por FEFO saltando lotes vencidos; si no alcanza, rechaza la operación. */
    @Transactional
    public void descontarStock(Long productoId, int cantidadADescontar, Kardex.TipoOperacion tipo, String detalle,
                               String docTipo, String docId) {
        if (cantidadADescontar <= 0) return;
        Producto producto = inventarioService.bloquearProducto(productoId);
        inventarioService.descontarFefo(producto, cantidadADescontar, tipo, detalle, docTipo, docId);
    }

    /** Descuenta unidades de un lote concreto (p. ej. el lote de vacuna aplicado) y registra el movimiento. */
    @Transactional
    public Lote descontarDeLote(Long loteId, int cantidad, Kardex.TipoOperacion tipo, String detalle,
                                String docTipo, String docId) {
        inventarioService.descontarDeLote(loteId, cantidad, false, tipo, detalle, docTipo, docId);
        return loteRepository.findById(loteId).orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
    }
}
