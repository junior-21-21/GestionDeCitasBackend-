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
import org.springframework.transaction.annotation.Transactional;
import com.farmacia.sistemaWeb.entity.Kardex;

import java.time.LocalDate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegistroVacunaService {

    @Autowired private RegistroVacunaRepository registroRepository;
    @Autowired private VacunaRepository vacunaRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private VeterinarioRepository veterinarioRepository;
    @Autowired private com.farmacia.sistemaWeb.repository.LoteRepository loteRepository;
    @Autowired private LoteService loteService;


    @Transactional
    public RegistroVacunaResponseDTO registrar(RegistroVacunaDTO dto) {
        if (dto.getFechaAplicacion().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("La fecha de aplicación no puede ser futura");
        }
        if (dto.getProximaDosis() != null && !dto.getProximaDosis().isAfter(dto.getFechaAplicacion())) {
            throw new BusinessRuleException("La próxima dosis debe ser posterior a la fecha de aplicación");
        }

        Vacuna vacuna = vacunaRepository.findById(dto.getVacunaId())
                .orElseThrow(() -> new ResourceNotFoundException("Vacuna no encontrada"));

        if (!vacuna.isActiva()) {
            throw new BusinessRuleException("No se puede aplicar una vacuna inactiva");
        }

        Paciente paciente = pacienteRepository.findByCodigoPaciente(dto.getPacienteCodigo())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        Veterinario veterinario = veterinarioRepository.findById(dto.getVeterinarioDni())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado"));

        // La vacuna debe estar indicada para la especie del paciente (si la vacuna tiene especies definidas)
        if (vacuna.getEspecies() != null && !vacuna.getEspecies().isEmpty()
                && paciente.getRaza() != null && paciente.getRaza().getEspecie() != null) {
            Long especiePaciente = paciente.getRaza().getEspecie().getId();
            boolean aplica = vacuna.getEspecies().stream().anyMatch(e -> e.getId().equals(especiePaciente));
            if (!aplica) {
                throw new BusinessRuleException("La vacuna " + vacuna.getNombre() + " no está indicada para la especie "
                        + paciente.getRaza().getEspecie().getNombre());
            }
        }

        RegistroVacuna registro = new RegistroVacuna();
        registro.setVacuna(vacuna);
        registro.setPaciente(paciente);
        registro.setVeterinario(veterinario);

        registro.setFechaAplicacion(dto.getFechaAplicacion());
        registro.setProximaDosis(dto.getProximaDosis());
        registro.setObservaciones(dto.getObservaciones());

        // El lote debe ser de una vacuna: antes se podia descontar, por ejemplo, una tableta de amoxicilina.
        if (dto.getLoteId() != null) {
            com.farmacia.sistemaWeb.entity.Lote lote = loteRepository.findById(dto.getLoteId())
                    .orElseThrow(() -> new BusinessRuleException("Lote no encontrado"));
            com.farmacia.sistemaWeb.entity.Producto prod = lote.getProducto();
            if (prod != null && prod.getClase() != null
                    && prod.getClase() != com.farmacia.sistemaWeb.entity.ClaseProducto.VACUNA) {
                throw new BusinessRuleException("El lote elegido no es de una vacuna (" + prod.getNombre() + ").");
            }
        }

        RegistroVacuna guardado = registroRepository.save(registro);

        // Antes loteId se recibía pero se ignoraba: ahora se valida y se descuenta una dosis del lote
        // (con bloqueo del producto y movimiento en el kardex con usuario y lote)
        if (dto.getLoteId() != null) {
            try {
                loteService.descontarDeLote(dto.getLoteId(), 1, Kardex.TipoOperacion.CONSUMO_INTERNO,
                        "Vacunación paciente " + paciente.getCodigoPaciente(), "VACUNA", String.valueOf(guardado.getId()));
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException(e.getMessage());
            }
        }
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
