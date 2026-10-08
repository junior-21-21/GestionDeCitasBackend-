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

    @Column(name = "estado_ingreso")
    private String estadoIngreso;

    @Column(name = "estado_salida")
    private String estadoSalida;

    @Column(name = "requiere_internamiento", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private boolean requiereInternamiento = false;

    @Column(name = "motivo_internamiento")
    private String motivoInternamiento;

    // SOAP - Nuevos campos de Signos Vitales y Triage
    @Column(name = "nivel_urgencia", length = 20)
    private String nivelUrgencia; // Normal, Urgencia, Emergencia

    @Column(name = "temperatura")
    private Double temperatura;

    @Column(name = "frecuencia_cardiaca")
    private Integer frecuenciaCardiaca;

    @Column(name = "frecuencia_respiratoria")
    private Integer frecuenciaRespiratoria;

    @Column(name = "tiempo_llenado_capilar")
    private Integer tiempoLlenadoCapilar;

    @Column(name = "sistemas_anormales", columnDefinition = "TEXT")
    private String sistemasAnormales; // JSON array o CSV de sistemas con hallazgos anormales

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

    public String getEstadoIngreso() {
        return estadoIngreso;
    }

    public void setEstadoIngreso(String estadoIngreso) {
        this.estadoIngreso = estadoIngreso;
    }

    public String getEstadoSalida() {
        return estadoSalida;
    }

    public void setEstadoSalida(String estadoSalida) {
        this.estadoSalida = estadoSalida;
    }

    public boolean isRequiereInternamiento() {
        return requiereInternamiento;
    }

    public void setRequiereInternamiento(boolean requiereInternamiento) {
        this.requiereInternamiento = requiereInternamiento;
    }

    public String getMotivoInternamiento() {
        return motivoInternamiento;
    }

    public void setMotivoInternamiento(String motivoInternamiento) {
        this.motivoInternamiento = motivoInternamiento;
    }

    public String getNivelUrgencia() {
        return nivelUrgencia;
    }

    public void setNivelUrgencia(String nivelUrgencia) {
        this.nivelUrgencia = nivelUrgencia;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Integer getFrecuenciaCardiaca() {
        return frecuenciaCardiaca;
    }

    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) {
        this.frecuenciaCardiaca = frecuenciaCardiaca;
    }

    public Integer getFrecuenciaRespiratoria() {
        return frecuenciaRespiratoria;
    }

    public void setFrecuenciaRespiratoria(Integer frecuenciaRespiratoria) {
        this.frecuenciaRespiratoria = frecuenciaRespiratoria;
    }

    public Integer getTiempoLlenadoCapilar() {
        return tiempoLlenadoCapilar;
    }

    public void setTiempoLlenadoCapilar(Integer tiempoLlenadoCapilar) {
        this.tiempoLlenadoCapilar = tiempoLlenadoCapilar;
    }

    public String getSistemasAnormales() {
        return sistemasAnormales;
    }

    public void setSistemasAnormales(String sistemasAnormales) {
        this.sistemasAnormales = sistemasAnormales;
    }
}
