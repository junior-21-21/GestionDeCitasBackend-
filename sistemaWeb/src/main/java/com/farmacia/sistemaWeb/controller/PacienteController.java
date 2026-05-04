package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.PacienteDTO;
import com.farmacia.sistemaWeb.dto.PacienteResponseDTO;
import com.farmacia.sistemaWeb.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> registrar(@RequestBody @jakarta.validation.Valid PacienteDTO dto) {
        PacienteResponseDTO paciente = pacienteService.registrarPaciente(dto);
        return ResponseEntity.ok(paciente);
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    @GetMapping("/{codigoPaciente}")
    public ResponseEntity<PacienteResponseDTO> obtenerPorCodigo(@PathVariable String codigoPaciente) {
        return ResponseEntity.ok(pacienteService.obtenerPorCodigo(codigoPaciente));
    }

    @GetMapping("/cliente/{clienteDni}")
    public ResponseEntity<List<PacienteResponseDTO>> listarPorCliente(@PathVariable String clienteDni) {
        return ResponseEntity.ok(pacienteService.obtenerPacientesPorCliente(clienteDni));
    }

    @GetMapping("/por-nombre/{nombre}")
    public ResponseEntity<List<PacienteResponseDTO>> buscarPorNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(pacienteService.buscarPacientesPorNombre(nombre));
    }

    @GetMapping("/por-dni/{dni}")
    public ResponseEntity<List<PacienteResponseDTO>> buscarPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(pacienteService.buscarPacientesPorDni(dni));
    }

    @DeleteMapping("/{codigoPaciente}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigoPaciente) {
        pacienteService.eliminarPaciente(codigoPaciente);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{codigoPaciente}")
    public ResponseEntity<PacienteResponseDTO> actualizar(@PathVariable String codigoPaciente,
            @RequestBody @jakarta.validation.Valid PacienteDTO dto) {
        PacienteResponseDTO pacienteActualizado = pacienteService.actualizarPaciente(codigoPaciente, dto);
        return ResponseEntity.ok(pacienteActualizado);
    }

    @GetMapping("/{codigoPaciente}/credencial/pdf")
    public ResponseEntity<org.springframework.core.io.Resource> generarCredencialPdf(
            @PathVariable String codigoPaciente) {
        byte[] pdfBytes = pacienteService.generarCredencialPdf(codigoPaciente);

        org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(
                pdfBytes);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"credencial_" + codigoPaciente + ".pdf\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }
}
