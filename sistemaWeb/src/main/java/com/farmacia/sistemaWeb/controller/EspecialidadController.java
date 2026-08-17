package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.EspecialidadDTO;
import com.farmacia.sistemaWeb.entity.Especialidad;
import com.farmacia.sistemaWeb.service.EspecialidadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadController {

    @Autowired
    private EspecialidadService especialidadService;

    @PostMapping
    public ResponseEntity<Especialidad> crear(@Valid @RequestBody EspecialidadDTO dto) {
        return ResponseEntity.ok(especialidadService.crear(dto));
    }

    @GetMapping
    public ResponseEntity<List<Especialidad>> listar() {
        return ResponseEntity.ok(especialidadService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Especialidad> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(especialidadService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        especialidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
