package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.RecetaMedica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecetaMedicaRepository extends JpaRepository<RecetaMedica, Long> {
    Optional<RecetaMedica> findByConsultaCodigoConsulta(String codigoConsulta);
}
