package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.CobroConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

@Repository
public interface CobroConsultaRepository extends JpaRepository<CobroConsulta, Long> {
    Optional<CobroConsulta> findByConsultaCodigoConsulta(String codigoConsulta);
    java.util.List<CobroConsulta> findByEstado(String estado);

    /** Bloquea el cobro mientras se paga: dos clics o dos cajas a la vez descontaban el stock dos veces. */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CobroConsulta c WHERE c.id = :id")
    Optional<CobroConsulta> findByIdParaActualizar(@Param("id") Long id);

    @Query("SELECT SUM(c.total) FROM CobroConsulta c WHERE c.estado = 'PAGADO' AND c.fecha BETWEEN :inicio AND :fin")
    Double sumTotalByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(c.total) FROM CobroConsulta c WHERE c.estado = 'PAGADO'")
    Double sumTotal();
}
