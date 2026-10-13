package com.farmacia.sistemaWeb.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class ConsultaDTO {

    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha de la consulta no puede ser futura")
    private LocalDate fecha;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    private String motivo;

    @Positive(message = "El peso debe ser mayor a cero")
    @Max(value = 1000, message = "El peso no puede superar los 1000 kg")
    private Double peso;
    @Size(max = 255, message = "Las observaciones no pueden superar los 255 caracteres")
    private String observaciones;
    @Size(max = 255, message = "El diagnóstico no puede superar los 255 caracteres")
    private String diagnostico;
    @Size(max = 255, message = "El tratamiento no puede superar los 255 caracteres")
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

    @Size(max = 255, message = "El estado de ingreso no puede superar los 255 caracteres")
    private String estadoIngreso;
    @Size(max = 255, message = "El estado de salida no puede superar los 255 caracteres")
    private String estadoSalida;
    private boolean requiereInternamiento;
    @Size(max = 255, message = "El motivo de internamiento no puede superar los 255 caracteres")
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
