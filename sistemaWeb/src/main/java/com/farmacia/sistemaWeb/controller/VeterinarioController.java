package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.VeterinarioDTO;
import com.farmacia.sistemaWeb.dto.VeterinarioResponseDTO;
import com.farmacia.sistemaWeb.service.VeterinarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioController {

    @Autowired
    private VeterinarioService veterinarioService;

    // ✅ Registrar veterinario
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody VeterinarioDTO dto) {
        try {
            VeterinarioResponseDTO respuesta = veterinarioService.registrarVeterinario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ✅ Listar veterinarios
    @GetMapping
    public ResponseEntity<List<VeterinarioResponseDTO>> listar() {
        return ResponseEntity.ok(veterinarioService.listarVeterinarios());
    }

    // ✅ Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(veterinarioService.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ Eliminar veterinario
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        veterinarioService.eliminar(id);
        return ResponseEntity.ok("Veterinario eliminado");
    }

    // ✅ Editar/Actualizar veterinario
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody VeterinarioDTO dto) {
        try {
            VeterinarioResponseDTO actualizado = veterinarioService.actualizarVeterinario(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
