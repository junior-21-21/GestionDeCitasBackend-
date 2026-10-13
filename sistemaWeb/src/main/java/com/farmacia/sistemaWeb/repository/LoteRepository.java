package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {

    // Obtener lotes de un producto que tengan stock, ordenados por fecha de vencimiento (los que vencen antes primero)
    @Query("SELECT l FROM Lote l WHERE l.producto.id = :productoId AND l.stockActual > 0 ORDER BY l.fechaVencimiento ASC, l.fechaIngreso ASC, l.id ASC")
    List<Lote> findLotesConStockOrderByFechaVencimiento(@Param("productoId") Long productoId);

    // FEFO solo con lotes vigentes: los vencidos no se venden ni se consumen
    @Query("SELECT l FROM Lote l WHERE l.producto.id = :productoId AND l.stockActual > 0 AND l.fechaVencimiento >= :hoy "
            + "ORDER BY l.fechaVencimiento ASC, l.fechaIngreso ASC, l.id ASC")
    List<Lote> findLotesVigentesFefo(@Param("productoId") Long productoId, @Param("hoy") LocalDate hoy);

    // Buscar todos los lotes de un producto
    List<Lote> findByProductoId(Long productoId);

    @Query("SELECT l FROM Lote l WHERE l.producto.id = :productoId ORDER BY CASE WHEN l.stockActual > 0 THEN 0 ELSE 1 END, l.fechaVencimiento ASC, l.id ASC")
    List<Lote> findByProductoIdOrdenados(@Param("productoId") Long productoId);

    @Query("SELECT COALESCE(SUM(l.stockActual), 0) FROM Lote l WHERE l.producto.id = :productoId")
    Long sumStockByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT l.producto.id FROM Lote l WHERE l.id = :loteId")
    Optional<Long> findProductoIdByLoteId(@Param("loteId") Long loteId);

    @Query("SELECT l FROM Lote l JOIN FETCH l.producto p WHERE l.stockActual > 0 AND l.fechaVencimiento >= :desde AND l.fechaVencimiento <= :hasta ORDER BY l.fechaVencimiento ASC")
    List<Lote> findConStockVenceEntre(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("SELECT l FROM Lote l JOIN FETCH l.producto p WHERE l.stockActual > 0 AND l.fechaVencimiento < :hoy ORDER BY l.fechaVencimiento ASC")
    List<Lote> findVencidosConStock(@Param("hoy") LocalDate hoy);

    boolean existsByProductoId(Long productoId);
}
