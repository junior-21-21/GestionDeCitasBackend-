package com.farmacia.sistemaWeb.repository;

import com.farmacia.sistemaWeb.entity.Vacuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacunaRepository extends JpaRepository<Vacuna, Long> {

    List<Vacuna> findByActivaTrue();

    /**
     * Busca vacunas activas que estén asociadas a una especie específica.
     * Reemplaza el antiguo findByEspecieDestinoAndActivaTrue.
     */
    @Query("SELECT v FROM Vacuna v JOIN v.especies e WHERE e.id = :especieId AND v.activa = true")
    List<Vacuna> findActivasByEspecieId(@Param("especieId") Long especieId);
}
