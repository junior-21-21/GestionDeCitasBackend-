package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByCodigo(String codigo);
}
