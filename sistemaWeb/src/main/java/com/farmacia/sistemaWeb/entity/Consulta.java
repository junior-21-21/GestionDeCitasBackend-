package com.farmacia.sistemaWeb.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @Column(name = "codigo_consulta", length = 30, nullable = false)
    private String codigoConsulta;

    private LocalDate fecha;
    private String motivo;
    private Double peso;
    private String observaciones;
    private String diagnostico;
    private String tratamiento;

    @ManyToOne
    @JoinColumn(name = "paciente_codigo", referencedColumnName = "codigo_paciente", nullable = true)
    @JsonIgnoreProperties({ "consultas", "cliente" })
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "veterinario_dni", referencedColumnName = "dni", nullable = true)
    @JsonIgnoreProperties({ "consultas", "citas" })
    private Veterinario veterinario;

    @OneToOne
    @JoinColumn(name = "cita_codigo", referencedColumnName = "codigo_cita", nullable = true)
    @JsonIgnoreProperties({ "consulta", "paciente", "veterinario" })
    private Cita cita;

    // Getters y Setters
    public String getCodigoConsulta() {
        return codigoConsulta;
    }

    public void setCodigoConsulta(String codigoConsulta) {
        this.codigoConsulta = codigoConsulta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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

    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }
}
