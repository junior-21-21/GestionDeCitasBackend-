package com.farmacia.sistemaWeb.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class ConsultaDTO {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private Double peso;
    private String observaciones;
    private String diagnostico;
    private String tratamiento;

    @NotBlank(message = "El código de cita es obligatorio")
    private String citaCodigo;

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getCitaCodigo() { return citaCodigo; }
    public void setCitaCodigo(String citaCodigo) { this.citaCodigo = citaCodigo; }

    private String estadoIngreso;
    private String estadoSalida;
    private boolean requiereInternamiento;
    private String motivoInternamiento;

    public String getEstadoIngreso() { return estadoIngreso; }
    public void setEstadoIngreso(String estadoIngreso) { this.estadoIngreso = estadoIngreso; }
    public String getEstadoSalida() { return estadoSalida; }
    public void setEstadoSalida(String estadoSalida) { this.estadoSalida = estadoSalida; }
    public boolean isRequiereInternamiento() { return requiereInternamiento; }
    public void setRequiereInternamiento(boolean requiereInternamiento) { this.requiereInternamiento = requiereInternamiento; }
    public String getMotivoInternamiento() { return motivoInternamiento; }
    public void setMotivoInternamiento(String motivoInternamiento) { this.motivoInternamiento = motivoInternamiento; }
}
