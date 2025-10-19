package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.ConsultaDTO;
import com.farmacia.sistemaWeb.dto.ConsultaMedicamentoDTO;
import com.farmacia.sistemaWeb.dto.ConsultaMedicamentoResponse;
import com.farmacia.sistemaWeb.dto.ConsultaResponseDTO;
import com.farmacia.sistemaWeb.entity.Consulta;
import com.farmacia.sistemaWeb.entity.ConsultaMedicamento;
import com.farmacia.sistemaWeb.service.ConsultaService;
import com.farmacia.sistemaWeb.service.ConsultaMedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaService consultaService;

    @Autowired
    private ConsultaMedicamentoService consultaMedicamentoService;

    // ✅ Registrar consulta
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody ConsultaDTO dto) {
        try {
            Consulta nueva = consultaService.registrarConsulta(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ✅ Asociar medicamento a consulta
    @PostMapping("/medicamento")
    public ResponseEntity<?> agregarMedicamento(@RequestBody ConsultaMedicamentoDTO dto) {
        try {
            ConsultaMedicamentoResponse resultado = consultaMedicamentoService.registrarMedicamentoEnConsulta(dto);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ Obtener medicamentos asociados (el que se usa en el frontend)
    @GetMapping("/{id}/medicamentos")
    public ResponseEntity<List<ConsultaMedicamentoResponse>> obtenerMedicamentos(@PathVariable Long id) {
        return ResponseEntity.ok(consultaMedicamentoService.obtenerPorConsultaId(id));
    }

    // ✅ Buscar por DNI cliente
    @GetMapping("/por-dni/{dni}")
    public ResponseEntity<List<ConsultaResponseDTO>> buscarPorDni(@PathVariable String dni) {
        List<Consulta> consultas = consultaService.buscarConsultasPorDniCliente(dni);

        List<ConsultaResponseDTO> respuesta = consultas.stream().map(c -> {
            ConsultaResponseDTO dto = new ConsultaResponseDTO();
            dto.setId(c.getId());
            dto.setFecha(c.getFecha() != null ? c.getFecha().toString() : "");
            dto.setMotivo(c.getMotivo());
            dto.setDiagnostico(c.getDiagnostico());
            dto.setTratamiento(c.getTratamiento());
            dto.setNombreMascota(c.getMascota() != null ? c.getMascota().getNombre() : "");
            dto.setNombreVeterinario(c.getVeterinario() != null ? c.getVeterinario().getNombres() : "");
            return dto;
        }).toList();

        return ResponseEntity.ok(respuesta);
    }

    // ✅ Listar todas las consultas
    @GetMapping
    public ResponseEntity<List<ConsultaResponseDTO>> listarTodas() {
        List<Consulta> consultas = consultaService.listarConsultas();

        List<ConsultaResponseDTO> respuesta = consultas.stream().map(c -> {
            ConsultaResponseDTO dto = new ConsultaResponseDTO();
            dto.setId(c.getId());
            dto.setFecha(c.getFecha() != null ? c.getFecha().toString() : "");
            dto.setMotivo(c.getMotivo());
            dto.setDiagnostico(c.getDiagnostico());
            dto.setTratamiento(c.getTratamiento());
            dto.setNombreMascota(c.getMascota() != null ? c.getMascota().getNombre() : "");
            dto.setNombreVeterinario(c.getVeterinario() != null ? c.getVeterinario().getNombres() : "");
            return dto;
        }).toList();

        return ResponseEntity.ok(respuesta);
    }
}