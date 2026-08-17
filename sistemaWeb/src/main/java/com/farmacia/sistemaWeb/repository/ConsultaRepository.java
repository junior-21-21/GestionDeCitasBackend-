package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, String> {
    List<Consulta> findByCitaPacienteCodigoPaciente(String codigoPaciente);

    List<Consulta> findByCitaVeterinarioDni(String veterinarioDni);

    List<Consulta> findByCitaPacienteClienteDni(String dni);

    List<Consulta> findByCitaPacienteCodigoPacienteOrderByFechaDesc(String codigoPaciente);

    List<Consulta> findByFecha(java.time.LocalDate fecha);

    java.util.Optional<Consulta> findByCitaCodigoCita(String codigoCita);

    long countByFecha(java.time.LocalDate fecha);
    
    long countByFechaBetween(java.time.LocalDate start, java.time.LocalDate end);
}