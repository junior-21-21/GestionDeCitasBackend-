package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByRuc(String ruc);
    List<Proveedor> findAllByOrderByRazonSocialAsc();
}
