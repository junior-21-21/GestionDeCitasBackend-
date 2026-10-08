package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {
    
    // Obtener lotes de un producto que tengan stock, ordenados por fecha de vencimiento (los que vencen antes primero)
    @Query("SELECT l FROM Lote l WHERE l.producto.id = :productoId AND l.stockActual > 0 ORDER BY l.fechaVencimiento ASC")
    List<Lote> findLotesConStockOrderByFechaVencimiento(Long productoId);
    
    // Buscar todos los lotes de un producto
    List<Lote> findByProductoId(Long productoId);
}
