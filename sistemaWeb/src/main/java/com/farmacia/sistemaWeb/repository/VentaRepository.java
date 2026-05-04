package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    long countByFecha(java.time.LocalDate fecha);

    java.util.Optional<Venta> findByCodigoVenta(String codigoVenta);
}
