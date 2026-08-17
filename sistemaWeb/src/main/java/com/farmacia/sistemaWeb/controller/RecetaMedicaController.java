package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.entity.RecetaMedica;
import com.farmacia.sistemaWeb.service.RecetaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recetas")
public class RecetaMedicaController {

    @Autowired
    private RecetaMedicaService recetaService;

    @PostMapping("/consulta/{codigoConsulta}")
    public ResponseEntity<?> generarReceta(@PathVariable String codigoConsulta, @RequestBody RecetaMedica receta) {
        try {
            RecetaMedica nuevaReceta = recetaService.generarReceta(codigoConsulta, receta);
            return ResponseEntity.ok(nuevaReceta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/consulta/{codigoConsulta}")
    public ResponseEntity<?> obtenerReceta(@PathVariable String codigoConsulta) {
        return recetaService.obtenerRecetaPorConsulta(codigoConsulta)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
