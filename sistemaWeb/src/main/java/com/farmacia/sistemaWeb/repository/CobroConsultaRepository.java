package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.CobroConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CobroConsultaRepository extends JpaRepository<CobroConsulta, Long> {
    Optional<CobroConsulta> findByConsultaCodigoConsulta(String codigoConsulta);
    java.util.List<CobroConsulta> findByEstado(String estado);
}
