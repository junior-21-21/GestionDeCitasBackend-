package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.ConsultaDTO;
import com.farmacia.sistemaWeb.entity.Cita;
import com.farmacia.sistemaWeb.entity.Consulta;
import com.farmacia.sistemaWeb.repository.CitaRepository;
import com.farmacia.sistemaWeb.repository.ConsultaRepository;
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
        if (dto.getCitaCodigo() == null || dto.getCitaCodigo().isEmpty()) {
            throw new RuntimeException("El código de cita es obligatorio. Las consultas requieren cita previa.");
        }

        Cita cita = citaRepository.findById(dto.getCitaCodigo())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        // Validar que la cita no esté ya asociada a otra consulta
        if (consultaRepository.findByCitaCodigoCita(dto.getCitaCodigo()).isPresent()) {
            throw new RuntimeException("La cita " + dto.getCitaCodigo() + " ya tiene una consulta asociada");
        }

        Consulta consulta = new Consulta();
        consulta.setCodigoConsulta(generarCodigoConsulta(dto.getFecha()));
        consulta.setFecha(dto.getFecha());
        consulta.setMotivo(dto.getMotivo());
        consulta.setPeso(dto.getPeso());
        consulta.setObservaciones(dto.getObservaciones());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        
        // 3FN: Asignar la cita. Paciente y veterinario se obtienen transitivamente.
        consulta.setCita(cita);

        // Actualizar estado de la cita a REALIZADA
        cita.setEstado(Cita.EstadoCita.REALIZADA);
        citaRepository.save(cita);

        Consulta guardada = consultaRepository.save(consulta);
        return mapToResponseDTO(guardada);
    }

    public List<ConsultaResponseDTO> listarConsultas() {
        return mapListToResponseDTO(consultaRepository.findAll());
    }

    public List<ConsultaResponseDTO> obtenerHistorialPorPaciente(String codigoPaciente) {
        return mapListToResponseDTO(consultaRepository.findByCitaPacienteCodigoPacienteOrderByFechaDesc(codigoPaciente));
    }

    public List<ConsultaResponseDTO> listarConsultasHoy() {
        return mapListToResponseDTO(consultaRepository.findByFecha(LocalDate.now()));
    }

    public Consulta buscarPorCodigo(String codigoConsulta) {
        return consultaRepository.findById(codigoConsulta)
                .orElseThrow(() -> new RuntimeException("Consulta no encontrada"));
    }

    public List<ConsultaResponseDTO> buscarConsultasPorDniCliente(String dni) {
        return mapListToResponseDTO(consultaRepository.findByCitaPacienteClienteDni(dni));
    }

    @Transactional
    public Consulta actualizarConsulta(String codigoConsulta, ConsultaDTO dto) {
        Consulta consulta = buscarPorCodigo(codigoConsulta);

        consulta.setFecha(dto.getFecha());
        consulta.setMotivo(dto.getMotivo());
        consulta.setPeso(dto.getPeso());
        consulta.setObservaciones(dto.getObservaciones());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());

        if (dto.getCitaCodigo() != null && !dto.getCitaCodigo().isEmpty() &&
            !dto.getCitaCodigo().equals(consulta.getCita().getCodigoCita())) {
            
            // Revertir la cita anterior si es necesario, o solo asignar la nueva
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
