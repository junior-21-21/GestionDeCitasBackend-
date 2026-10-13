package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.LoteDTO;
import com.farmacia.sistemaWeb.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    @Autowired
    private InventarioService inventarioService;

    /** Lotes de un producto con su estado de vencimiento (VIGENTE, POR_VENCER, VENCIDO, AGOTADO). */
    @GetMapping("/producto/{productoId}")
    public List<LoteDTO> porProducto(@PathVariable Long productoId) {
        return inventarioService.lotesDeProducto(productoId);
    }

    /** Lotes con stock que vencen en los próximos {dias} días (30 por defecto). */
    @GetMapping("/por-vencer")
    public List<LoteDTO> porVencer(@RequestParam(defaultValue = "30") int dias) {
        return inventarioService.lotesPorVencer(dias);
    }

    /** Lotes vencidos que aún tienen stock. */
    @GetMapping("/vencidos")
    public List<LoteDTO> vencidos() {
        return inventarioService.lotesVencidos();
    }

    /** Da de baja todo el stock de un lote vencido (ajuste por vencimiento con su movimiento en el kardex). */
    @PostMapping("/{id}/baja")
    public ResponseEntity<?> darDeBaja(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String observacion = body != null ? body.get("observacion") : null;
        try {
            return ResponseEntity.ok(inventarioService.ajusteComoMapa(inventarioService.darDeBajaLote(id, observacion)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
