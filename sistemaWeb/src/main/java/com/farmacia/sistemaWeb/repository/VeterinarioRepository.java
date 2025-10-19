package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {
}