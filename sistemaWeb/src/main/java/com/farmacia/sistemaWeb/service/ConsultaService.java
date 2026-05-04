package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.ConsultaDTO;
import com.farmacia.sistemaWeb.entity.Cita;
import com.farmacia.sistemaWeb.entity.Consulta;
import com.farmacia.sistemaWeb.entity.Paciente;
import com.farmacia.sistemaWeb.entity.Veterinario;
import com.farmacia.sistemaWeb.repository.CitaRepository;
import com.farmacia.sistemaWeb.repository.ConsultaRepository;
import com.farmacia.sistemaWeb.repository.PacienteRepository;
import com.farmacia.sistemaWeb.repository.VeterinarioRepository;
import com.farmacia.sistemaWeb.dto.ConsultaResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ConsultaService {

    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private VeterinarioRepository veterinarioRepository;
    @Autowired
    private ConsultaRepository consultaRepository;

    private String generarCodigoConsulta(LocalDate fecha) {
        String fechaStr = fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<Consulta> consultasHoy = consultaRepository.findByFecha(fecha);
        int maxSuffix = 0;
        for (Consulta c : consultasHoy) {
            try {
                String codigo = c.getCodigoConsulta();
                if (codigo != null && codigo.contains("-")) {
                    String suffixStr = codigo.substring(codigo.lastIndexOf("-") + 1);
                    int suffix = Integer.parseInt(suffixStr);
                    if (suffix > maxSuffix) {
                        maxSuffix = suffix;
                    }
                }
            } catch (Exception e) {
                // Ignorar si el formato no coincide
            }
        }
        return String.format("CON-%s-%03d", fechaStr, maxSuffix + 1);
    }

    public ConsultaResponseDTO mapToResponseDTO(Consulta c) {
        ConsultaResponseDTO dto = new ConsultaResponseDTO();
        dto.setCodigoConsulta(c.getCodigoConsulta());
        dto.setFecha(c.getFecha() != null ? c.getFecha().toString() : "");
        dto.setMotivo(c.getMotivo());
        dto.setDiagnostico(c.getDiagnostico());
        dto.setTratamiento(c.getTratamiento());
        dto.setNombrePaciente(c.getPaciente() != null ? c.getPaciente().getNombre() : "");
        dto.setNombreVeterinario(c.getVeterinario() != null ? c.getVeterinario().getNombres() : "");
        return dto;
    }

    private List<ConsultaResponseDTO> mapListToResponseDTO(List<Consulta> consultas) {
        return consultas.stream().map(this::mapToResponseDTO).toList();
    }

    @Transactional
    public ConsultaResponseDTO registrarConsulta(ConsultaDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.getPacienteCodigo())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Veterinario veterinario = veterinarioRepository.findById(dto.getVeterinarioDni())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        Consulta consulta = new Consulta();
        consulta.setCodigoConsulta(generarCodigoConsulta(dto.getFecha()));
        consulta.setFecha(dto.getFecha());
        consulta.setMotivo(dto.getMotivo());
        consulta.setPeso(dto.getPeso());
        consulta.setObservaciones(dto.getObservaciones());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        consulta.setPaciente(paciente);
        consulta.setVeterinario(veterinario);

        if (dto.getCitaCodigo() != null && !dto.getCitaCodigo().isEmpty()) {
            Cita cita = citaRepository.findById(dto.getCitaCodigo())
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
            consulta.setCita(cita);

            // Actualizar estado de la cita a REALIZADA
            cita.setEstado(Cita.EstadoCita.REALIZADA);
            citaRepository.save(cita);
        }

        Consulta guardada = consultaRepository.save(consulta);
        return mapToResponseDTO(guardada);
    }

    public List<ConsultaResponseDTO> listarConsultas() {
        return mapListToResponseDTO(consultaRepository.findAll());
    }

    public List<ConsultaResponseDTO> obtenerHistorialPorPaciente(String codigoPaciente) {
        return mapListToResponseDTO(consultaRepository.findByPacienteCodigoPacienteOrderByFechaDesc(codigoPaciente));
    }

    public List<ConsultaResponseDTO> listarConsultasHoy() {
        return mapListToResponseDTO(consultaRepository.findByFecha(LocalDate.now()));
    }

    public Consulta buscarPorCodigo(String codigoConsulta) {
        return consultaRepository.findById(codigoConsulta)
                .orElseThrow(() -> new RuntimeException("Consulta no encontrada"));
    }

    public List<ConsultaResponseDTO> buscarConsultasPorDniCliente(String dni) {
        return mapListToResponseDTO(consultaRepository.findByPacienteClienteDni(dni));
    }

    public Consulta actualizarConsulta(String codigoConsulta, ConsultaDTO dto) {
        Consulta consulta = buscarPorCodigo(codigoConsulta);

        Paciente paciente = pacienteRepository.findById(dto.getPacienteCodigo())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Veterinario veterinario = veterinarioRepository.findById(dto.getVeterinarioDni())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        consulta.setFecha(dto.getFecha());
        consulta.setMotivo(dto.getMotivo());
        consulta.setPeso(dto.getPeso());
        consulta.setObservaciones(dto.getObservaciones());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        consulta.setPaciente(paciente);
        consulta.setVeterinario(veterinario);

        if (dto.getCitaCodigo() != null && !dto.getCitaCodigo().isEmpty()) {
            Cita cita = citaRepository.findById(dto.getCitaCodigo())
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
            consulta.setCita(cita);
        }

        return consultaRepository.save(consulta);
    }

    public void eliminarConsulta(String codigoConsulta) {
        Consulta consulta = buscarPorCodigo(codigoConsulta);
        consultaRepository.delete(consulta);
    }
}
