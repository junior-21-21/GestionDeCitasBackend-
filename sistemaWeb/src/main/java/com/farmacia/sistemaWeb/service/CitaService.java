package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.CitaDTO;
import com.farmacia.sistemaWeb.dto.CitaResponseDTO;
import com.farmacia.sistemaWeb.entity.Cita;
import com.farmacia.sistemaWeb.entity.Paciente;
import com.farmacia.sistemaWeb.entity.Veterinario;
import com.farmacia.sistemaWeb.repository.CitaRepository;
import com.farmacia.sistemaWeb.repository.ConsultaRepository;
import com.farmacia.sistemaWeb.repository.PacienteRepository;
import com.farmacia.sistemaWeb.repository.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private VeterinarioRepository veterinarioRepository;
    @Autowired
    private ConsultaRepository consultaRepository;

    private String generarCodigoCita(LocalDate fecha) {
        String fechaStr = fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = citaRepository.countByFecha(fecha);
        return String.format("CIT-%s-%03d", fechaStr, count + 1);
    }

    public Cita registrarCita(CitaDTO dto) {
        if (dto.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException("No se pueden registrar citas en fechas pasadas.");
        }

        validarTraslape(dto.getVeterinarioDni(), dto.getFecha(), dto.getHora(), dto.getDuracionMinutos(), null);

        Paciente paciente = pacienteRepository.findById(dto.getPacienteCodigo())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Veterinario vet = veterinarioRepository.findById(dto.getVeterinarioDni())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        Cita c = new Cita();
        c.setCodigoCita(generarCodigoCita(dto.getFecha()));
        c.setFecha(dto.getFecha());
        c.setHora(dto.getHora());
        c.setMotivo(dto.getMotivo());
        c.setDuracionMinutos(dto.getDuracionMinutos() != null ? dto.getDuracionMinutos() : 30);
        c.setEstado(Cita.EstadoCita.PENDIENTE);
        c.setPaciente(paciente);
        c.setVeterinario(vet);

        return citaRepository.save(c);
    }

    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    public List<Cita> listarPorEstado(Cita.EstadoCita estado) {
        return citaRepository.findByEstado(estado);
    }

    public List<Cita> listarPorVeterinario(String vetDni) {
        return citaRepository.findByVeterinarioDni(vetDni);
    }

    public Cita cambiarEstado(String codigoCita, Cita.EstadoCita estado) {
        Cita c = citaRepository.findById(codigoCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (Cita.EstadoCita.REALIZADA.equals(c.getEstado()) && !Cita.EstadoCita.REALIZADA.equals(estado)) {
            throw new RuntimeException("No se puede modificar una cita que ya ha sido atendida.");
        }

        c.setEstado(estado);
        return citaRepository.save(c);
    }

    public List<CitaResponseDTO> listarDTO() {
        return citaRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public List<CitaResponseDTO> listarDTOPorVeterinario(String vetDni) {
        return citaRepository.findByVeterinarioDni(vetDni).stream().map(this::mapToDTO).toList();
    }

    private CitaResponseDTO mapToDTO(Cita c) {
        CitaResponseDTO dto = new CitaResponseDTO();
        dto.setCodigoCita(c.getCodigoCita());
        dto.setFecha(c.getFecha());
        dto.setHora(c.getHora());
        dto.setMotivo(c.getMotivo());
        dto.setEstado(c.getEstado() != null ? c.getEstado().name() : "");
        dto.setNombrePaciente(c.getPaciente().getNombre());
        dto.setNombreVeterinario(c.getVeterinario().getNombres());
        dto.setPacienteCodigo(c.getPaciente().getCodigoPaciente());
        dto.setVeterinarioDni(c.getVeterinario().getDni());
        dto.setDuracionMinutos(c.getDuracionMinutos());

        // Buscar consulta asociada para obtener el código
        consultaRepository.findByCitaCodigoCita(c.getCodigoCita())
                .ifPresent(consulta -> dto.setCodigoConsulta(consulta.getCodigoConsulta()));

        return dto;
    }

    public Cita editarCita(String codigoCita, CitaDTO dto) {
        Cita cita = citaRepository.findById(codigoCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (Cita.EstadoCita.REALIZADA.equals(cita.getEstado())) {
            throw new RuntimeException("No se puede editar una cita atendida.");
        }

        if (dto.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException("La nueva fecha no puede ser en el pasado.");
        }

        validarTraslape(dto.getVeterinarioDni(), dto.getFecha(), dto.getHora(), dto.getDuracionMinutos(), codigoCita);

        Paciente paciente = pacienteRepository.findById(dto.getPacienteCodigo())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Veterinario vet = veterinarioRepository.findById(dto.getVeterinarioDni())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());
        cita.setMotivo(dto.getMotivo());
        cita.setDuracionMinutos(dto.getDuracionMinutos() != null ? dto.getDuracionMinutos() : 30);
        cita.setPaciente(paciente);
        cita.setVeterinario(vet);
        return citaRepository.save(cita);
    }

    public void eliminar(String codigoCita) {
        citaRepository.deleteById(codigoCita);
    }

    public CitaResponseDTO obtenerPorCodigoDTO(String codigoCita) {
        Cita c = citaRepository.findById(codigoCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        return mapToDTO(c);
    }

    private void validarTraslape(String veterinarioDni, java.time.LocalDate fecha, java.time.LocalTime nuevaHoraInicio,
            Integer duracionMinutos, String citaCodigoExcluir) {
        if (duracionMinutos == null)
            duracionMinutos = 30;

        java.time.LocalTime nuevaHoraFin = nuevaHoraInicio.plusMinutes(duracionMinutos);
        List<Cita> citasDelDia = citaRepository.findByVeterinarioDniAndFecha(veterinarioDni, fecha);

        for (Cita existente : citasDelDia) {
            if (citaCodigoExcluir != null && existente.getCodigoCita().equals(citaCodigoExcluir))
                continue;
            if (Cita.EstadoCita.CANCELADA.equals(existente.getEstado()))
                continue;

            java.time.LocalTime extInicio = existente.getHora();
            Integer extDuracion = existente.getDuracionMinutos() != null ? existente.getDuracionMinutos() : 30;
            java.time.LocalTime extFin = extInicio.plusMinutes(extDuracion);

            if (nuevaHoraInicio.isBefore(extFin) && nuevaHoraFin.isAfter(extInicio)) {
                throw new RuntimeException(
                        "El horario seleccionado se cruza con otra cita (" + extInicio + " - " + extFin + ")");
            }
        }
    }
}