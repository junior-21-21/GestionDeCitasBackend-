package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByProductoCodigoBarrasOrderByFechaHoraDesc(String codigoBarras);

    List<MovimientoInventario> findByLoteIdOrderByFechaHoraDesc(Long loteId);

    List<MovimientoInventario> findByProductoIsControladoTrueOrderByFechaHoraDesc();
}
