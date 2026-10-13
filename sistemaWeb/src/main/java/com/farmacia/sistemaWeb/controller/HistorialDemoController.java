package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.service.HistorialDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Historial de demostración (solo ADMIN, ver SecurityConfig):
 * <ul>
 *   <li>GET    /api/demo/historial → {generado: true|false}</li>
 *   <li>POST   /api/demo/historial → genera 30 días de uso simulado (409 si ya existe)</li>
 *   <li>DELETE /api/demo/historial → borra exactamente lo generado (409 si hay datos reales que dependen de ello)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/demo/historial")
public class HistorialDemoController {

    @Autowired
    private HistorialDemoService historialDemoService;

    @GetMapping
    public Map<String, Object> estado() {
        return Map.of("generado", historialDemoService.yaGenerado());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> generar() {
        try {
            return ResponseEntity.ok(historialDemoService.generar());
        } catch (HistorialDemoService.ConflictoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> eliminar() {
        try {
            return ResponseEntity.ok(historialDemoService.eliminar());
        } catch (HistorialDemoService.ConflictoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
    }
}
