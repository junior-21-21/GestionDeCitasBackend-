package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.ProductoDTO;
import com.farmacia.sistemaWeb.entity.CategoriaProducto;
import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.repository.CategoriaProductoRepository;
import com.farmacia.sistemaWeb.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaProductoRepository categoriaRepository;

    public List<ProductoDTO> listarTodos() {
        return productoRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ProductoDTO obtenerPorCodigo(String codigoBarras) {
        Producto producto = productoRepository.findById(codigoBarras)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado."));
        return toDTO(producto);
    }

    public ProductoDTO guardar(ProductoDTO dto) {
        if (dto.getCodigoBarras() == null || dto.getCodigoBarras().isBlank()) {
            throw new RuntimeException("El código de barras es obligatorio.");
        }
        Producto producto = new Producto();
        return toDTO(productoRepository.save(mapToEntity(dto, producto)));
    }

    public ProductoDTO actualizar(String codigoBarras, ProductoDTO dto) {
        Producto producto = productoRepository.findById(codigoBarras)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado."));
        return toDTO(productoRepository.save(mapToEntity(dto, producto)));
    }

    public void eliminar(String codigoBarras) {
        productoRepository.deleteById(codigoBarras);
    }

    private Producto mapToEntity(ProductoDTO dto, Producto entity) {
        entity.setCodigoBarras(dto.getCodigoBarras());
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setPrecioCompra(dto.getPrecioCompra());
        entity.setPrecioVenta(dto.getPrecioVenta());
        entity.setStockActual(dto.getStockActual());
        entity.setStockMinimo(dto.getStockMinimo());
        if (dto.getTipoInventario() != null) {
            try {
                entity.setTipoInventario(
                        com.farmacia.sistemaWeb.entity.TipoInventario.valueOf(dto.getTipoInventario()));
            } catch (IllegalArgumentException e) {
                entity.setTipoInventario(com.farmacia.sistemaWeb.entity.TipoInventario.PETSHOP);
            }
        }
        // TODO: add isControlado mapping if needed

        if (dto.getCategoriaId() != null) {
            CategoriaProducto categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada."));
            entity.setCategoria(categoria);
        } else {
            entity.setCategoria(null);
        }

        return entity;
    }

    private ProductoDTO toDTO(Producto entity) {
        ProductoDTO dto = new ProductoDTO();
        dto.setCodigoBarras(entity.getCodigoBarras());
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        dto.setPrecioCompra(entity.getPrecioCompra());
        dto.setPrecioVenta(entity.getPrecioVenta());
        dto.setStockActual(entity.getStockActual());
        dto.setStockMinimo(entity.getStockMinimo());
        if (entity.getTipoInventario() != null) {
            dto.setTipoInventario(entity.getTipoInventario().name());
        }
        if (entity.getCategoria() != null) {
            dto.setCategoriaId(entity.getCategoria().getId());
        }
        return dto;
    }
}
