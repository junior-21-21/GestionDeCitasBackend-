package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, String> {
    List<Cita> findByEstado(Cita.EstadoCita estado);

    List<Cita> findByVeterinarioDni(String veterinarioDni);

    List<Cita> findByVeterinarioDniAndFecha(String veterinarioDni, java.time.LocalDate fecha);

    long countByFecha(java.time.LocalDate fecha);
    
    long countByFechaBetween(java.time.LocalDate start, java.time.LocalDate end);
}