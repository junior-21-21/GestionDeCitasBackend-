package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.CitaDTO;
import com.farmacia.sistemaWeb.dto.CitaResponseDTO;
import com.farmacia.sistemaWeb.entity.Cita;
import com.farmacia.sistemaWeb.entity.Mascota;
import com.farmacia.sistemaWeb.entity.Veterinario;
import com.farmacia.sistemaWeb.repository.CitaRepository;
import com.farmacia.sistemaWeb.repository.MascotaRepository;
import com.farmacia.sistemaWeb.repository.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private MascotaRepository mascotaRepository;
    @Autowired
    private VeterinarioRepository veterinarioRepository;

    public Cita registrarCita(CitaDTO dto) {
        Mascota mascota = mascotaRepository.findById(dto.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        Veterinario vet = veterinarioRepository.findById(dto.getVeterinarioId())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        Cita c = new Cita();
        c.setFecha(dto.getFecha());
        c.setHora(dto.getHora());
        c.setMotivo(dto.getMotivo());
        c.setEstado("PENDIENTE");
        c.setMascota(mascota);
        c.setVeterinario(vet);

        return citaRepository.save(c);
    }

    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    public List<Cita> listarPorEstado(String estado) {
        return citaRepository.findByEstado(estado);
    }

    public List<Cita> listarPorVeterinario(Long vetId) {
        return citaRepository.findByVeterinarioId(vetId);
    }

    public Cita cambiarEstado(Long id, String estado) {
        Cita c = citaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        c.setEstado(estado);
        return citaRepository.save(c);
    }

    public List<CitaResponseDTO> listarDTO() {
        return citaRepository.findAll().stream().map(c -> {
            CitaResponseDTO dto = new CitaResponseDTO();
            dto.setId(c.getId());
            dto.setFecha(c.getFecha());
            dto.setHora(c.getHora());
            dto.setMotivo(c.getMotivo());
            dto.setEstado(c.getEstado());
            dto.setNombreMascota(c.getMascota().getNombre());
            dto.setNombreVeterinario(c.getVeterinario().getNombres());
            return dto;
        }).toList();
    }

    public Cita editarCita(Long id, CitaDTO dto) {
        Cita cita = citaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        Mascota mascota = mascotaRepository.findById(dto.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        Veterinario vet = veterinarioRepository.findById(dto.getVeterinarioId())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());
        cita.setMotivo(dto.getMotivo());
        cita.setMascota(mascota);
        cita.setVeterinario(vet);
        return citaRepository.save(cita);
    }

}