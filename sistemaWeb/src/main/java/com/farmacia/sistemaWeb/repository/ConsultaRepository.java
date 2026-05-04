package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, String> {
    List<Consulta> findByPacienteCodigoPaciente(String codigoPaciente);

    List<Consulta> findByVeterinarioDni(String veterinarioDni);

    List<Consulta> findByPacienteClienteDni(String dni);

    List<Consulta> findByPacienteCodigoPacienteOrderByFechaDesc(String codigoPaciente);

    List<Consulta> findByFecha(java.time.LocalDate fecha);

    java.util.Optional<Consulta> findByCitaCodigoCita(String codigoCita);

    long countByFecha(java.time.LocalDate fecha);
}