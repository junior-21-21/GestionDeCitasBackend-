package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin AND (v.anulada IS NULL OR v.anulada = false)")
    Double sumTotalByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.anulada IS NULL OR v.anulada = false")
    Double sumTotal();

    @Query("SELECT COUNT(d) > 0 FROM DetalleVenta d WHERE d.producto.id = :productoId")
    boolean existsDetalleConProducto(@Param("productoId") Long productoId);
}
