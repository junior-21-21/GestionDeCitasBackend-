package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    @Query("SELECT SUM(c.total) FROM Compra c WHERE c.fechaRegistro BETWEEN :inicio AND :fin")
    BigDecimal sumTotalByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(c.total) FROM Compra c")
    BigDecimal sumTotal();
}
