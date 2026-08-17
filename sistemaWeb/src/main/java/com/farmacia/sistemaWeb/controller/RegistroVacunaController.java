package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.RegistroVacunaDTO;
import com.farmacia.sistemaWeb.dto.RegistroVacunaResponseDTO;
import com.farmacia.sistemaWeb.service.RegistroVacunaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registro-vacunas")
public class RegistroVacunaController {

    @Autowired
    private RegistroVacunaService registroVacunaService;

    @PostMapping
    public ResponseEntity<RegistroVacunaResponseDTO> registrar(@Valid @RequestBody RegistroVacunaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroVacunaService.registrar(dto));
    }

    @GetMapping("/paciente/{codigoPaciente}")
    public ResponseEntity<List<RegistroVacunaResponseDTO>> listarPorPaciente(@PathVariable String codigoPaciente) {
        return ResponseEntity.ok(registroVacunaService.listarPorPaciente(codigoPaciente));
    }
}
