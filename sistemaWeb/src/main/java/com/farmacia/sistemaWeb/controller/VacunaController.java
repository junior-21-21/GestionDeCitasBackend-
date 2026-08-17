package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.VacunaDTO;
import com.farmacia.sistemaWeb.entity.Vacuna;
import com.farmacia.sistemaWeb.service.VacunaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacunas")
public class VacunaController {

    @Autowired
    private VacunaService vacunaService;

    @PostMapping
    public ResponseEntity<Vacuna> crear(@Valid @RequestBody VacunaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vacunaService.crear(dto));
    }

    @GetMapping
    public ResponseEntity<List<Vacuna>> listarTodas() {
        return ResponseEntity.ok(vacunaService.listarTodas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<Vacuna>> listarActivas() {
        return ResponseEntity.ok(vacunaService.listarActivas());
    }

    @GetMapping("/por-especie/{especieId}")
    public ResponseEntity<List<Vacuna>> listarPorEspecie(@PathVariable Long especieId) {
        return ResponseEntity.ok(vacunaService.listarPorEspecie(especieId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vacuna> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(vacunaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vacuna> actualizar(@PathVariable Long id, @Valid @RequestBody VacunaDTO dto) {
        return ResponseEntity.ok(vacunaService.actualizar(id, dto));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestParam boolean activa) {
        vacunaService.cambiarEstado(id, activa);
        return ResponseEntity.noContent().build();
    }
}
