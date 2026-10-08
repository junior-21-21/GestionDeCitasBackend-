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
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Compra> listarCompras() {
        return compraService.listarCompras();
    }
}
