package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    boolean existsByDni(String dni);

    Optional<Cliente> findByDni(String dni);

    List<Cliente> findByDniContaining(String dni);
}