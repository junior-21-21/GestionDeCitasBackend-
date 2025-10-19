package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.MascotaDTO;
import com.farmacia.sistemaWeb.dto.MascotaResponseDTO;
import com.farmacia.sistemaWeb.entity.Mascota;
import com.farmacia.sistemaWeb.repository.MascotaRepository;
import com.farmacia.sistemaWeb.service.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    @Autowired
    private MascotaService mascotaService;

    @Autowired
    private MascotaRepository mascotaRepository;

    // ✅ Registrar mascota
    @PostMapping
    public ResponseEntity<MascotaResponseDTO> registrar(@RequestBody MascotaDTO dto) {
        MascotaResponseDTO mascota = mascotaService.registrarMascota(dto);
        return ResponseEntity.ok(mascota);
    }

    // ✅ Listar todas las mascotas
    @GetMapping
    public ResponseEntity<List<MascotaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(mascotaService.listarTodas());
    }

    // ✅ Listar mascotas por cliente
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<MascotaResponseDTO>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(mascotaService.obtenerMascotasPorCliente(clienteId));
    }

    // ✅ Buscar mascotas por nombre (autocompletar o búsqueda parcial)
    @GetMapping("/por-nombre/{nombre}")
    public ResponseEntity<List<Mascota>> buscarPorNombre(@PathVariable String nombre) {
        List<Mascota> mascotas = mascotaRepository.findByNombreContainingIgnoreCase(nombre);
        return ResponseEntity.ok(mascotas);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mascotaService.eliminarMascota(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<MascotaResponseDTO> actualizar(@PathVariable Long id, @RequestBody MascotaDTO dto) {
        MascotaResponseDTO mascotaActualizada = mascotaService.actualizarMascota(id, dto);
        return ResponseEntity.ok(mascotaActualizada);
    }



}
