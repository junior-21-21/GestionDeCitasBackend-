package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.entity.Compra;
import com.farmacia.sistemaWeb.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping
    public ResponseEntity<?> registrarCompra(@RequestBody Compra compra) {
        try {
            return ResponseEntity.ok(compraService.registrarCompra(compra));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    /** Anula la compra: retira de sus lotes lo que ingresó. Falla si esas unidades ya se usaron o si ya estaba anulada. */
    @PostMapping("/{id}/anular")
    public ResponseEntity<?> anularCompra(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, String> body) {
        try {
            return ResponseEntity.ok(compraService.anularCompra(id, body != null ? body.get("motivo") : null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping
    public List<Compra> listarCompras() {
        return compraService.listarCompras();
    }
}
