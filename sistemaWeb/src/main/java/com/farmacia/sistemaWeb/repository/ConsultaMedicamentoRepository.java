package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.ConsultaMedicamento;
import com.farmacia.sistemaWeb.entity.ConsultaMedicamentoPK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultaMedicamentoRepository extends JpaRepository<ConsultaMedicamento, ConsultaMedicamentoPK> {
    List<ConsultaMedicamento> findByConsultaId(Long consultaId);
}
