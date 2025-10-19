package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
}
