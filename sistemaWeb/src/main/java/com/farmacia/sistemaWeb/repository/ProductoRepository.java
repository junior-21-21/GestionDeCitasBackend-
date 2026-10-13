package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByCodigo(String codigo);
    java.util.Optional<Producto> findByCodigo(String codigo);

    /** Bloquea la fila del producto hasta el fin de la transacción: dos ventas simultáneas no venden la misma unidad. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdParaActualizar(@Param("id") Long id);
}
