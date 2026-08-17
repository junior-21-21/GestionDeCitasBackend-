package com.farmacia.sistemaWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * RegistroVacuna — Cada aplicación de una vacuna a un paciente.
 * Almacena la fecha de aplicación, el veterinario que la aplicó,
 * el lote real del inventario, y la fecha recomendada para la próxima dosis.
 */
@Entity
@Table(name = "registro_vacunas")
public class RegistroVacuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vacuna_id", nullable = false)
    private Vacuna vacuna;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_codigo", referencedColumnName = "codigo_paciente", nullable = false)
    @JsonIgnoreProperties({"cliente"})
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "veterinario_dni", referencedColumnName = "dni", nullable = false)
    @JsonIgnoreProperties({"especialidad", "usuario"})
    private Veterinario veterinario;


    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDate fechaAplicacion;

    @Column(name = "proxima_dosis")
    private LocalDate proximaDosis;

    @Column(length = 500)
    private String observaciones;

    // ── Getters y Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vacuna getVacuna() { return vacuna; }
    public void setVacuna(Vacuna vacuna) { this.vacuna = vacuna; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Veterinario getVeterinario() { return veterinario; }
    public void setVeterinario(Veterinario veterinario) { this.veterinario = veterinario; }


    public LocalDate getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(LocalDate fechaAplicacion) { this.fechaAplicacion = fechaAplicacion; }

    public LocalDate getProximaDosis() { return proximaDosis; }
    public void setProximaDosis(LocalDate proximaDosis) { this.proximaDosis = proximaDosis; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
