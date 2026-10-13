package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.AjusteRequestDTO;
import com.farmacia.sistemaWeb.dto.InventarioResumenDTO;
import com.farmacia.sistemaWeb.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    /** Ajuste de stock (entrada o salida) con motivo obligatorio. Queda en el kardex con el usuario. */
    @PostMapping("/ajustes")
    public ResponseEntity<?> registrarAjuste(@RequestBody AjusteRequestDTO request) {
        try {
            return ResponseEntity.ok(inventarioService.ajusteComoMapa(inventarioService.registrarAjuste(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping("/ajustes")
    public List<Map<String, Object>> listarAjustes(@RequestParam(required = false) Long productoId) {
        return inventarioService.listarAjustes(productoId);
    }

    /** Motivos de ajuste disponibles. */
    @GetMapping("/motivos")
    public Map<String, String> motivos() {
        return InventarioService.MOTIVOS;
    }

    /** Valor del inventario (stock × costo promedio) y alertas de stock bajo y vencimientos. */
    @GetMapping("/resumen")
    public InventarioResumenDTO resumen() {
        return inventarioService.resumen();
    }

    /** Iguala el stock de cada producto con la suma de sus lotes (idempotente). */
    @PostMapping("/conciliar")
    public Map<String, Object> conciliar() {
        int corregidos = inventarioService.conciliar();
        return Map.of("corregidos", corregidos, "mensaje", "Se conciliaron " + corregidos + " productos con sus lotes.");
    }
}
