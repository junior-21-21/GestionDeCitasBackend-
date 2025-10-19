package com.farmacia.sistemaWeb.repository;



import com.farmacia.sistemaWeb.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByMascotaId(Long mascotaId);
    List<Consulta> findByVeterinarioId(Long veterinarioId);
    List<Consulta> findByMascotaClienteDni(String dni);

}