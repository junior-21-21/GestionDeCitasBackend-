package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VeterinarioRepository extends JpaRepository<Veterinario, String> {
    Optional<Veterinario> findByUsuarioEmail(String email);

    Optional<Veterinario> findByCorreo(String correo);
}