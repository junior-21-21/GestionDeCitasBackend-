package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.HorarioTrabajador;
import com.farmacia.sistemaWeb.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioTrabajadorRepository extends JpaRepository<HorarioTrabajador, Long> {
    List<HorarioTrabajador> findByUsuario(Usuario usuario);
}
