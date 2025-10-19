package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByClienteId(Long clienteId);
    List<Mascota> findByNombreContainingIgnoreCase(String nombre);

}
