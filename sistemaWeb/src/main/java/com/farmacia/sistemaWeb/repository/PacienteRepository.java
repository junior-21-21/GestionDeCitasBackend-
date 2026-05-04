package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, String> {
    List<Paciente> findByClienteDni(String dni);

    List<Paciente> findByNombreContainingIgnoreCase(String nombre);

    long countByNombreStartingWithIgnoreCase(String prefijo);
}
