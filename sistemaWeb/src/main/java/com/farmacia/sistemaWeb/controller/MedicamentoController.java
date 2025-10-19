package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.MedicamentoDTO;
import com.farmacia.sistemaWeb.entity.Medicamento;
import com.farmacia.sistemaWeb.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentoController {

    @Autowired
    private MedicamentoService medicamentoService;

    @PostMapping
    public ResponseEntity<Medicamento> crear(@RequestBody MedicamentoDTO dto) {
        return ResponseEntity.ok(medicamentoService.crearMedicamento(dto));
    }

    @GetMapping
    public List<Medicamento> listar() {
        return medicamentoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Medicamento obtener(@PathVariable Long id) {
        return medicamentoService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public Medicamento actualizar(@PathVariable Long id, @RequestBody MedicamentoDTO dto) {
        return medicamentoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        medicamentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
