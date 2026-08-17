package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
