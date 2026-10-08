package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.AsistenciaDiaria;
import com.farmacia.sistemaWeb.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AsistenciaDiariaRepository extends JpaRepository<AsistenciaDiaria, Long> {
    Optional<AsistenciaDiaria> findByUsuarioAndFechaAndEstado(Usuario usuario, LocalDate fecha, AsistenciaDiaria.EstadoAsistencia estado);
    Optional<AsistenciaDiaria> findByUsuarioAndFecha(Usuario usuario, LocalDate fecha);
}
