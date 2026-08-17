package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.VacunaDTO;
import com.farmacia.sistemaWeb.entity.Especie;
import com.farmacia.sistemaWeb.entity.Vacuna;
import com.farmacia.sistemaWeb.repository.EspecieRepository;
import com.farmacia.sistemaWeb.repository.VacunaRepository;
import com.farmacia.sistemaWeb.util.GlobalExceptionHandler.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VacunaService {

    @Autowired
    private VacunaRepository vacunaRepository;

    @Autowired
    private EspecieRepository especieRepository;

    public Vacuna crear(VacunaDTO dto) {
        Vacuna vacuna = new Vacuna();
        mapearDesdeDTO(vacuna, dto);
        vacuna.setActiva(true);
        return vacunaRepository.save(vacuna);
    }

    public List<Vacuna> listarTodas() {
        return vacunaRepository.findAll();
    }

    public List<Vacuna> listarActivas() {
        return vacunaRepository.findByActivaTrue();
    }

    public List<Vacuna> listarPorEspecie(Long especieId) {
        return vacunaRepository.findActivasByEspecieId(especieId);
    }

    public Vacuna obtenerPorId(Long id) {
        return vacunaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacuna no encontrada con ID: " + id));
    }

    public Vacuna actualizar(Long id, VacunaDTO dto) {
        Vacuna vacuna = obtenerPorId(id);
        mapearDesdeDTO(vacuna, dto);
        return vacunaRepository.save(vacuna);
    }

    public void cambiarEstado(Long id, boolean activa) {
        Vacuna vacuna = obtenerPorId(id);
        vacuna.setActiva(activa);
        vacunaRepository.save(vacuna);
    }

    /**
     * Mapea campos del DTO a la entidad, incluyendo la resolución
     * de especieIds a entidades Especie reales.
     */
    private void mapearDesdeDTO(Vacuna vacuna, VacunaDTO dto) {
        vacuna.setNombre(dto.getNombre());
        vacuna.setFabricante(dto.getFabricante());
        vacuna.setDescripcion(dto.getDescripcion());
        vacuna.setPeriodicidadMeses(dto.getPeriodicidadMeses());

        // Convertir String tipo a Enum
        if (dto.getTipo() != null && !dto.getTipo().isBlank()) {
            try {
                vacuna.setTipo(Vacuna.TipoVacuna.valueOf(dto.getTipo().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Tipo de vacuna inválido: '" + dto.getTipo()
                                + "'. Valores permitidos: OBLIGATORIA, OPCIONAL, REFUERZO");
            }
        }

        // Resolver especies desde IDs
        if (dto.getEspecieIds() != null && !dto.getEspecieIds().isEmpty()) {
            List<Especie> especies = especieRepository.findAllById(dto.getEspecieIds());
            if (especies.size() != dto.getEspecieIds().size()) {
                throw new ResourceNotFoundException("Una o más especies no fueron encontradas");
            }
            vacuna.setEspecies(especies);
        } else {
            vacuna.setEspecies(new ArrayList<>());
        }
    }
}
