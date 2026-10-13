package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Veterinario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VeterinarioRepository extends JpaRepository<Veterinario, String> {
    Optional<Veterinario> findByUsuarioEmail(String email);

    Optional<Veterinario> findByCorreo(String correo);

    /** Bloquea la fila del veterinario para serializar el registro de citas y evitar traslapes concurrentes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Veterinario v WHERE v.dni = :dni")
    Optional<Veterinario> findByDniParaActualizar(@Param("dni") String dni);
}
