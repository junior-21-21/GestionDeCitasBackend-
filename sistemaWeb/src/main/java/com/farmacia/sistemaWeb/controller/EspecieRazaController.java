package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.entity.Especie;
import com.farmacia.sistemaWeb.entity.Raza;
import com.farmacia.sistemaWeb.service.EspecieRazaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/especies")
public class EspecieRazaController {

    @Autowired
    private EspecieRazaService especieRazaService;

    @GetMapping
    public ResponseEntity<List<Especie>> listarEspecies() {
        return ResponseEntity.ok(especieRazaService.listarEspecies());
    }

    @GetMapping("/{id}/razas")
    public ResponseEntity<List<Raza>> listarRazasPorEspecie(@PathVariable Long id) {
        return ResponseEntity.ok(especieRazaService.listarRazasPorEspecie(id));
    }

    @PostMapping
    public ResponseEntity<?> crearEspecie(@RequestBody Map<String, String> body) {
        try {
            String nombre = body.get("nombre");
            if (nombre == null || nombre.isBlank()) {
                return ResponseEntity.badRequest().body("El nombre de la especie es obligatorio");
            }
            Especie especie = especieRazaService.crearEspecie(nombre);
            return ResponseEntity.status(HttpStatus.CREATED).body(especie);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/razas")
    public ResponseEntity<?> crearRaza(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String nombre = body.get("nombre");
            if (nombre == null || nombre.isBlank()) {
                return ResponseEntity.badRequest().body("El nombre de la raza es obligatorio");
            }
            Raza raza = especieRazaService.crearRaza(id, nombre);
            return ResponseEntity.status(HttpStatus.CREATED).body(raza);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
