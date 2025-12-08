package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.CitaDTO;
import com.farmacia.sistemaWeb.dto.CitaResponseDTO;
import com.farmacia.sistemaWeb.entity.Cita;
import com.farmacia.sistemaWeb.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody CitaDTO dto) {
        try {
            Cita cita = citaService.registrarCita(dto);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public List<Cita> listarTodas() {
        return citaService.listarTodas();
    }

    @GetMapping("/pendientes")
    public List<Cita> listarPendientes() {
        return citaService.listarPorEstado("PENDIENTE");
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        try {
            return ResponseEntity.ok(citaService.cambiarEstado(id, estado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/veterinario/{id}")
    public List<Cita> listarPorVeterinario(@PathVariable Long id) {
        return citaService.listarPorVeterinario(id);
    }

    @GetMapping("/resumen")
    public List<CitaResponseDTO> listarResumen() {
        return citaService.listarDTO();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarCita(@PathVariable Long id, @RequestBody CitaDTO dto) {
        try {
            return ResponseEntity.ok(citaService.editarCita(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCita(@PathVariable Long id) {
        try {
            citaService.eliminar(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(citaService.obtenerPorIdDTO(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
