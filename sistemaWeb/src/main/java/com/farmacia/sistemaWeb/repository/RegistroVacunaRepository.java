package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.RegistroVacuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroVacunaRepository extends JpaRepository<RegistroVacuna, Long> {
    List<RegistroVacuna> findByPaciente_CodigoPacienteOrderByFechaAplicacionDesc(String codigoPaciente);
}
