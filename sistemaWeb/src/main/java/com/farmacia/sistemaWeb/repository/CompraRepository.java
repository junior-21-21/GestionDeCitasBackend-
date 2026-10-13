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

    @Query("SELECT SUM(c.total) FROM Compra c WHERE c.fechaRegistro BETWEEN :inicio AND :fin AND (c.anulada IS NULL OR c.anulada = false)")
    BigDecimal sumTotalByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(c.total) FROM Compra c WHERE c.anulada IS NULL OR c.anulada = false")
    BigDecimal sumTotal();

    @Query("SELECT COUNT(c) > 0 FROM Compra c WHERE c.proveedor.id = :proveedorId")
    boolean existsByProveedorId(@Param("proveedorId") Long proveedorId);

    @Query("SELECT COUNT(d) > 0 FROM DetalleCompra d WHERE d.producto.id = :productoId")
    boolean existsDetalleConProducto(@Param("productoId") Long productoId);

    @Query("SELECT c FROM Compra c ORDER BY c.fechaRegistro DESC, c.id DESC")
    java.util.List<Compra> findAllRecientes();
}
