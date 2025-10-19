package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByEstado(String estado);
    List<Cita> findByVeterinarioId(Long veterinarioId);
}