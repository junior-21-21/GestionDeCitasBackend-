package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.MedicamentoDTO;
import com.farmacia.sistemaWeb.entity.Medicamento;
import com.farmacia.sistemaWeb.repository.MedicamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicamentoService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    public Medicamento crearMedicamento(MedicamentoDTO dto) {
        Medicamento m = new Medicamento();
        m.setNombre(dto.getNombre());
        m.setDescripcion(dto.getDescripcion());
        m.setStock(dto.getStock());
        m.setPrecio(dto.getPrecio());
        return medicamentoRepository.save(m);
    }

    public List<Medicamento> listarTodos() {
        return medicamentoRepository.findAll();
    }

    public Medicamento obtenerPorId(Long id) {
        return medicamentoRepository.findById(id).orElseThrow(() -> new RuntimeException("No encontrado"));
    }

    public Medicamento actualizar(Long id, MedicamentoDTO dto) {
        Medicamento m = obtenerPorId(id);
        m.setNombre(dto.getNombre());
        m.setDescripcion(dto.getDescripcion());
        m.setStock(dto.getStock());
        m.setPrecio(dto.getPrecio());
        return medicamentoRepository.save(m);
    }

    public void eliminar(Long id) {
        medicamentoRepository.deleteById(id);
    }
}
