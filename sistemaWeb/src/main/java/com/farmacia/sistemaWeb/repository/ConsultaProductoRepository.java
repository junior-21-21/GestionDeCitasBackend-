package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.ConsultaProducto;
import com.farmacia.sistemaWeb.entity.ConsultaProductoPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaProductoRepository extends JpaRepository<ConsultaProducto, ConsultaProductoPK> {
    List<ConsultaProducto> findByConsultaCodigoConsulta(String codigoConsulta);
}
