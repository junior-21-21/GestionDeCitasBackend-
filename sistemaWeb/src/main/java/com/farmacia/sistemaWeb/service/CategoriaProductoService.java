package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.CategoriaProductoDTO;
import com.farmacia.sistemaWeb.entity.CategoriaProducto;
import com.farmacia.sistemaWeb.repository.CategoriaProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaProductoService {

    @Autowired
    private CategoriaProductoRepository repository;

    public List<CategoriaProductoDTO> listarTodos() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CategoriaProductoDTO obtenerPorId(Long id) {
        CategoriaProducto categoria = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada."));
        return toDTO(categoria);
    }

    public CategoriaProductoDTO guardar(CategoriaProductoDTO dto) {
        CategoriaProducto categoria = new CategoriaProducto();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        return toDTO(repository.save(categoria));
    }

    public CategoriaProductoDTO actualizar(Long id, CategoriaProductoDTO dto) {
        CategoriaProducto categoria = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada."));
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        return toDTO(repository.save(categoria));
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    private CategoriaProductoDTO toDTO(CategoriaProducto entity) {
        CategoriaProductoDTO dto = new CategoriaProductoDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        return dto;
    }
}
