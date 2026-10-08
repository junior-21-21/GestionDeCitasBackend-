package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Kardex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KardexRepository extends JpaRepository<Kardex, Long> {
    List<Kardex> findByProductoIdOrderByFechaHoraDesc(Long productoId);
}
