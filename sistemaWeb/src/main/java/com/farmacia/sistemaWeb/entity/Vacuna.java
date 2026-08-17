package com.farmacia.sistemaWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Vacuna — Catálogo de vacunas disponibles en la clínica.
 * Cada vacuna define el tipo, fabricante y periodicidad recomendada.
 *
 * Integridad: vinculada a Producto del inventario mediante FK,
 * garantizando que los lotes aplicados correspondan al producto correcto.
 */
@Entity
@Table(name = "vacunas")
public class Vacuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 150)
    private String fabricante;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoVacuna tipo;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "periodicidad_meses")
    private Integer periodicidadMeses;


    /**
     * Relación ManyToMany con Especie — reemplaza el antiguo campo
     * "especie_destino" (String) para cumplir 3FN.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "vacuna_especie",
            joinColumns = @JoinColumn(name = "vacuna_id"),
            inverseJoinColumns = @JoinColumn(name = "especie_id")
    )
    @JsonIgnoreProperties({"razas"})
    private List<Especie> especies = new ArrayList<>();

    @Column(nullable = false)
    private boolean activa = true;

    // ── Enum ──

    public enum TipoVacuna {
        OBLIGATORIA, OPCIONAL, REFUERZO
    }

    // ── Getters y Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }

    public TipoVacuna getTipo() { return tipo; }
    public void setTipo(TipoVacuna tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getPeriodicidadMeses() { return periodicidadMeses; }
    public void setPeriodicidadMeses(Integer periodicidadMeses) { this.periodicidadMeses = periodicidadMeses; }


    public List<Especie> getEspecies() { return especies; }
    public void setEspecies(List<Especie> especies) { this.especies = especies; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
