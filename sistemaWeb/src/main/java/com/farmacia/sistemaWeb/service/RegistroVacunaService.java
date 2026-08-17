package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.RegistroVacunaDTO;
import com.farmacia.sistemaWeb.dto.RegistroVacunaResponseDTO;
import com.farmacia.sistemaWeb.entity.Paciente;
import com.farmacia.sistemaWeb.entity.RegistroVacuna;
import com.farmacia.sistemaWeb.entity.Vacuna;
import com.farmacia.sistemaWeb.entity.Veterinario;

import com.farmacia.sistemaWeb.repository.PacienteRepository;
import com.farmacia.sistemaWeb.repository.RegistroVacunaRepository;
import com.farmacia.sistemaWeb.repository.VacunaRepository;
import com.farmacia.sistemaWeb.repository.VeterinarioRepository;
import com.farmacia.sistemaWeb.util.GlobalExceptionHandler.BusinessRuleException;
import com.farmacia.sistemaWeb.util.GlobalExceptionHandler.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegistroVacunaService {

    @Autowired private RegistroVacunaRepository registroRepository;
    @Autowired private VacunaRepository vacunaRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private VeterinarioRepository veterinarioRepository;


    public RegistroVacunaResponseDTO registrar(RegistroVacunaDTO dto) {
        Vacuna vacuna = vacunaRepository.findById(dto.getVacunaId())
                .orElseThrow(() -> new ResourceNotFoundException("Vacuna no encontrada"));

        if (!vacuna.isActiva()) {
            throw new BusinessRuleException("No se puede aplicar una vacuna inactiva");
        }

        Paciente paciente = pacienteRepository.findByCodigoPaciente(dto.getPacienteCodigo())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        Veterinario veterinario = veterinarioRepository.findById(dto.getVeterinarioDni())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado"));



        RegistroVacuna registro = new RegistroVacuna();
        registro.setVacuna(vacuna);
        registro.setPaciente(paciente);
        registro.setVeterinario(veterinario);

        registro.setFechaAplicacion(dto.getFechaAplicacion());
        registro.setProximaDosis(dto.getProximaDosis());
        registro.setObservaciones(dto.getObservaciones());

        RegistroVacuna guardado = registroRepository.save(registro);
        return mapearADTO(guardado);
    }

    public List<RegistroVacunaResponseDTO> listarPorPaciente(String codigoPaciente) {
        List<RegistroVacuna> registros = registroRepository
                .findByPaciente_CodigoPacienteOrderByFechaAplicacionDesc(codigoPaciente);
        return registros.stream().map(this::mapearADTO).collect(Collectors.toList());
    }

    private RegistroVacunaResponseDTO mapearADTO(RegistroVacuna registro) {
        RegistroVacunaResponseDTO dto = new RegistroVacunaResponseDTO();
        dto.setId(registro.getId());
        dto.setVacunaNombre(registro.getVacuna().getNombre());
        dto.setVacunaTipo(registro.getVacuna().getTipo() != null
                ? registro.getVacuna().getTipo().name() : null);
        dto.setPacienteNombre(registro.getPaciente().getNombre());
        dto.setPacienteCodigo(registro.getPaciente().getCodigoPaciente());
        dto.setVeterinarioNombre(registro.getVeterinario().getNombres()
                + " " + registro.getVeterinario().getApellidos());
        dto.setVeterinarioDni(registro.getVeterinario().getDni());
        dto.setFechaAplicacion(registro.getFechaAplicacion());
        dto.setProximaDosis(registro.getProximaDosis());
        dto.setObservaciones(registro.getObservaciones());

        return dto;
    }
}
