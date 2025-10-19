package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.VeterinarioDTO;
import com.farmacia.sistemaWeb.dto.VeterinarioResponseDTO;
import com.farmacia.sistemaWeb.entity.Especialidad;
import com.farmacia.sistemaWeb.entity.Veterinario;
import com.farmacia.sistemaWeb.repository.EspecialidadRepository;
import com.farmacia.sistemaWeb.repository.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VeterinarioService {

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    // ✅ Registrar veterinario
    public VeterinarioResponseDTO registrarVeterinario(VeterinarioDTO dto) {
        Especialidad especialidad = especialidadRepository.findById(dto.getEspecialidadId())
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

        Veterinario v = new Veterinario();
        v.setNombres(dto.getNombres());
        v.setCmp(dto.getCmp());
        v.setEspecialidad(especialidad);

        v = veterinarioRepository.save(v);
        return convertirAVeterinarioResponseDTO(v);
    }

    // ✅ Listar todos
    public List<VeterinarioResponseDTO> listarVeterinarios() {
        return veterinarioRepository.findAll().stream()
                .map(this::convertirAVeterinarioResponseDTO)
                .collect(Collectors.toList());
    }

    // ✅ Buscar por ID
    public VeterinarioResponseDTO obtenerPorId(Long id) {
        Veterinario v = veterinarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
        return convertirAVeterinarioResponseDTO(v);
    }

    // ✅ Eliminar por ID
    public void eliminar(Long id) {
        veterinarioRepository.deleteById(id);
    }

    // ✅ Convertir entidad a DTO limpio (sin bucles)
    private VeterinarioResponseDTO convertirAVeterinarioResponseDTO(Veterinario v) {
        VeterinarioResponseDTO dto = new VeterinarioResponseDTO();
        dto.setId(v.getId());
        dto.setNombres(v.getNombres());
        dto.setCmp(v.getCmp());
        if (v.getEspecialidad() != null) {
            dto.setEspecialidad(v.getEspecialidad().getNombre());
        }
        return dto;
    }

    // ✅ Actualizar veterinario
    public VeterinarioResponseDTO actualizarVeterinario(Long id, VeterinarioDTO dto) {
        Veterinario veterinario = veterinarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado con ID: " + id));

        Especialidad especialidad = especialidadRepository.findById(dto.getEspecialidadId())
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

        veterinario.setNombres(dto.getNombres());
        veterinario.setCmp(dto.getCmp());
        veterinario.setEspecialidad(especialidad);

        veterinario = veterinarioRepository.save(veterinario);
        return convertirAVeterinarioResponseDTO(veterinario);
    }

}
