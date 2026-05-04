package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {
    List<Lote> findByProductoCodigoBarras(String codigoBarras);

    List<Lote> findByProductoCodigoBarrasAndStockActualGreaterThanOrderByFechaVencimientoAsc(String codigoBarras,
            int stockActual);
}
