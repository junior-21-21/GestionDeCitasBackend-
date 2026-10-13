package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Presentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PresentacionRepository extends JpaRepository<Presentacion, Long> {

    @Query("SELECT p FROM Presentacion p WHERE p.producto.id = :productoId ORDER BY p.factor ASC, p.id ASC")
    List<Presentacion> findByProductoIdOrdenadas(@Param("productoId") Long productoId);

    @Query("SELECT p FROM Presentacion p JOIN FETCH p.producto WHERE p.codigoBarras = :codigo")
    List<Presentacion> findByCodigoBarras(@Param("codigo") String codigo);

    boolean existsByCodigoBarras(String codigoBarras);

    @Query("SELECT COUNT(p) > 0 FROM Presentacion p WHERE p.producto.id = :productoId")
    boolean existsByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT p FROM Presentacion p WHERE p.producto.id = :productoId AND p.factor = 1 AND p.activo = true ORDER BY p.id ASC")
    List<Presentacion> findBaseDeProducto(@Param("productoId") Long productoId);

    default Optional<Presentacion> baseDeProducto(Long productoId) {
        return findBaseDeProducto(productoId).stream().findFirst();
    }
}
