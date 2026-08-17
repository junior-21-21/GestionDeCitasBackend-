package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.CitaDTO;
import com.farmacia.sistemaWeb.dto.CitaResponseDTO;
import com.farmacia.sistemaWeb.entity.Cita;
import com.farmacia.sistemaWeb.service.CitaService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> registrar(@Valid @RequestBody CitaDTO dto) {
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
        return citaService.listarPorEstado(Cita.EstadoCita.PENDIENTE);
    }

    @PutMapping("/{codigoCita}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable String codigoCita, @RequestParam String estado) {
        try {
            Cita.EstadoCita estadoEnum = Cita.EstadoCita.valueOf(estado.toUpperCase());
            return ResponseEntity.ok(citaService.cambiarEstado(codigoCita, estadoEnum));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Estado inválido: " + estado + ". Usar: PENDIENTE, REALIZADA, CANCELADA");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/veterinario/{dni}")
    public List<Cita> listarPorVeterinario(@PathVariable String dni) {
        return citaService.listarPorVeterinario(dni);
    }

    @GetMapping("/resumen")
    public List<CitaResponseDTO> listarResumen() {
        return citaService.listarDTO();
    }

    @GetMapping("/resumen/veterinario/{dni}")
    public List<CitaResponseDTO> listarResumenPorVeterinario(@PathVariable String dni) {
        return citaService.listarDTOPorVeterinario(dni);
    }

    @PutMapping("/{codigoCita}")
    public ResponseEntity<?> editarCita(@PathVariable String codigoCita, @Valid @RequestBody CitaDTO dto) {
        try {
            return ResponseEntity.ok(citaService.editarCita(codigoCita, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{codigoCita}")
    public ResponseEntity<?> eliminarCita(@PathVariable String codigoCita) {
        try {
            citaService.eliminar(codigoCita);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{codigoCita}")
    public ResponseEntity<?> obtenerPorCodigo(@PathVariable String codigoCita) {
        try {
            return ResponseEntity.ok(citaService.obtenerPorCodigoDTO(codigoCita));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
