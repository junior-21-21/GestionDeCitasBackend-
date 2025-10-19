package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.EspecialidadDTO;
import com.farmacia.sistemaWeb.entity.Especialidad;
import com.farmacia.sistemaWeb.repository.EspecialidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EspecialidadService {

    @Autowired
    private EspecialidadRepository especialidadRepository;

    public Especialidad crear(EspecialidadDTO dto) {
        Especialidad e = new Especialidad();
        e.setNombre(dto.getNombre());
        return especialidadRepository.save(e);
    }

    public List<Especialidad> listar() {
        return especialidadRepository.findAll();
    }

    public Especialidad obtenerPorId(Long id) {
        return especialidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
    }

    public void eliminar(Long id) {
        especialidadRepository.deleteById(id);
    }
}
