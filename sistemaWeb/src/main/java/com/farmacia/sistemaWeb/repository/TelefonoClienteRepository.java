package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.TelefonoCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelefonoClienteRepository extends JpaRepository<TelefonoCliente, Long> {
    List<TelefonoCliente> findByClienteDni(String clienteDni);
    void deleteByClienteDni(String clienteDni);
}
