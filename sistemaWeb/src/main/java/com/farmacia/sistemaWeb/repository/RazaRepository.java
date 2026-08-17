package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RazaRepository extends JpaRepository<Raza, Long> {
    List<Raza> findByEspecieId(Long especieId);
    Optional<Raza> findByNombreIgnoreCaseAndEspecieId(String nombre, Long especieId);
    boolean existsByNombreIgnoreCaseAndEspecieId(String nombre, Long especieId);
}
