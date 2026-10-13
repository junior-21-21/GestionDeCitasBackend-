package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Kardex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KardexRepository extends JpaRepository<Kardex, Long> {
    List<Kardex> findByProductoIdOrderByFechaHoraDesc(Long productoId);

    @Query("SELECT k FROM Kardex k LEFT JOIN FETCH k.lote LEFT JOIN FETCH k.usuario WHERE k.producto.id = :productoId ORDER BY k.fechaHora DESC, k.id DESC")
    List<Kardex> findHistorialConDetalle(@Param("productoId") Long productoId);

    @Query("SELECT k FROM Kardex k LEFT JOIN FETCH k.lote WHERE k.documentoTipo = :tipo AND k.documentoId = :id ORDER BY k.id ASC")
    List<Kardex> findByDocumento(@Param("tipo") String tipo, @Param("id") String id);

    boolean existsByProductoId(Long productoId);
}
