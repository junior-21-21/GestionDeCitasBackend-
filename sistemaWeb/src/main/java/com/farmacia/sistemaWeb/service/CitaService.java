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

import java.time.LocalDate;
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
        // VALIDACIÓN: Fecha no puede ser pasada
        if (dto.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException("No se pueden registrar citas en fechas pasadas.");
        }

        // VALIDACIÓN DE TRASLAPE (OVERLAP)
        validarTraslape(dto.getVeterinarioId(), dto.getFecha(), dto.getHora(), dto.getDuracionMinutos(), null);

        Mascota mascota = mascotaRepository.findById(dto.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        Veterinario vet = veterinarioRepository.findById(dto.getVeterinarioId())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        Cita c = new Cita();
        c.setFecha(dto.getFecha());
        c.setHora(dto.getHora());
        c.setMotivo(dto.getMotivo());
        c.setDuracionMinutos(dto.getDuracionMinutos() != null ? dto.getDuracionMinutos() : 30); // Default 30 min
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

        // VALIDACIÓN: Bloqueo si ya fue atendida
        if ("REALIZADA".equals(c.getEstado()) && !"REALIZADA".equals(estado)) {
            throw new RuntimeException("No se puede modificar una cita que ya ha sido atendida.");
        }

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
            dto.setMascotaId(c.getMascota().getId());
            dto.setVeterinarioId(c.getVeterinario().getId());
            dto.setDuracionMinutos(c.getDuracionMinutos());
            return dto;
        }).toList();
    }

    public Cita editarCita(Long id, CitaDTO dto) {
        Cita cita = citaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        // VALIDACIÓN: Bloqueo si ya fue atendida
        if ("REALIZADA".equals(cita.getEstado())) {
            throw new RuntimeException("No se puede editar una cita atendida.");
        }

        // VALIDACIÓN: Fecha no puede ser pasada
        if (dto.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException("La nueva fecha no puede ser en el pasado.");
        }

        // VALIDACIÓN DE TRASLAPE (Excluyendo la cita actual)
        validarTraslape(dto.getVeterinarioId(), dto.getFecha(), dto.getHora(), dto.getDuracionMinutos(), id);

        Mascota mascota = mascotaRepository.findById(dto.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        Veterinario vet = veterinarioRepository.findById(dto.getVeterinarioId())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());
        cita.setMotivo(dto.getMotivo());
        cita.setDuracionMinutos(dto.getDuracionMinutos() != null ? dto.getDuracionMinutos() : 30);
        cita.setMascota(mascota);
        cita.setVeterinario(vet);
        return citaRepository.save(cita);
    }

    public void eliminar(Long id) {
        citaRepository.deleteById(id);
    }

    public CitaResponseDTO obtenerPorIdDTO(Long id) {
        Cita c = citaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        CitaResponseDTO dto = new CitaResponseDTO();
        dto.setId(c.getId());
        dto.setFecha(c.getFecha());
        dto.setHora(c.getHora());
        dto.setMotivo(c.getMotivo());
        dto.setEstado(c.getEstado());
        dto.setNombreMascota(c.getMascota().getNombre());
        dto.setNombreVeterinario(c.getVeterinario().getNombres());
        dto.setMascotaId(c.getMascota().getId());
        dto.setVeterinarioId(c.getVeterinario().getId());
        dto.setDuracionMinutos(c.getDuracionMinutos());
        return dto;
    }

    private void validarTraslape(Long veterinarioId, java.time.LocalDate fecha, java.time.LocalTime nuevaHoraInicio,
            Integer duracionMinutos, Long citaIdExcluir) {
        if (duracionMinutos == null)
            duracionMinutos = 30;

        java.time.LocalTime nuevaHoraFin = nuevaHoraInicio.plusMinutes(duracionMinutos);
        List<Cita> citasDelDia = citaRepository.findByVeterinarioIdAndFecha(veterinarioId, fecha);

        for (Cita existente : citasDelDia) {
            // Ignorar la misma cita si estamos editando
            if (citaIdExcluir != null && existente.getId().equals(citaIdExcluir))
                continue;
            // Ignorar citas canceladas
            if ("CANCELADA".equals(existente.getEstado()))
                continue;

            java.time.LocalTime extInicio = existente.getHora();
            Integer extDuracion = existente.getDuracionMinutos() != null ? existente.getDuracionMinutos() : 30;
            java.time.LocalTime extFin = extInicio.plusMinutes(extDuracion);

            // Lógica de intersección: (StartA < EndB) && (EndA > StartB)
            if (nuevaHoraInicio.isBefore(extFin) && nuevaHoraFin.isAfter(extInicio)) {
                throw new RuntimeException(
                        "El horario seleccionado se cruza con otra cita (" + extInicio + " - " + extFin + ")");
            }
        }
    }
}