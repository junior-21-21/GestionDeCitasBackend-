package com.farmacia.sistemaWeb.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @Column(name = "codigo_cita", length = 30, nullable = false)
    private String codigoCita;

    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private Integer duracionMinutos;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoCita estado;

    @ManyToOne
    @JoinColumn(name = "paciente_codigo", referencedColumnName = "codigo_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "veterinario_dni", referencedColumnName = "dni", nullable = false)
    private Veterinario veterinario;

    // Enum para estado
    public enum EstadoCita {
        PENDIENTE, REALIZADA, CANCELADA
    }

    // Getters y Setters
    public String getCodigoCita() {
        return codigoCita;
    }

    public void setCodigoCita(String codigoCita) {
        this.codigoCita = codigoCita;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Veterinario getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(Veterinario veterinario) {
        this.veterinario = veterinario;
    }
}
