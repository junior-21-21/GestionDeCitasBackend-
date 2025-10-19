package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Rol;
import com.farmacia.sistemaWeb.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByRoles_Nombre(Rol.NombreRol nombre);
}
