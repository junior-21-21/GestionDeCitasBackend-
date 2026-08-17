package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface PacienteRepository extends JpaRepository<Paciente, String> {
    List<Paciente> findByClienteDni(String dni);

    List<Paciente> findByNombreContainingIgnoreCase(String nombre);

    long countByNombreStartingWithIgnoreCase(String prefijo);

    Optional<Paciente> findByCodigoPaciente(String codigoPaciente);
    
    @Query("SELECT p.raza.especie.nombre, COUNT(p) FROM Paciente p GROUP BY p.raza.especie.nombre ORDER BY COUNT(p) DESC")
    List<Object[]> findTopEspecies();
}
