package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.AjusteInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AjusteInventarioRepository extends JpaRepository<AjusteInventario, Long> {
    List<AjusteInventario> findByProductoIdOrderByFechaDesc(Long productoId);
    List<AjusteInventario> findTop100ByOrderByFechaDesc();
}
