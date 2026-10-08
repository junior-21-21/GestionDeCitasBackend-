package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.entity.Venta;
import com.farmacia.sistemaWeb.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public List<Venta> listarVentas() {
        return ventaService.listarVentas();
    }

    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody Venta ventaRequest) {
        try {
            Venta nuevaVenta = ventaService.registrarVentaDirecta(ventaRequest);
            return ResponseEntity.ok(nuevaVenta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ocurrió un error al procesar la venta");
        }
    }
}
