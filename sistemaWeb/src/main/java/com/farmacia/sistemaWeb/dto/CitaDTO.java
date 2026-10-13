package com.farmacia.sistemaWeb.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public class CitaDTO {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    private String motivo;

    @NotBlank(message = "El código del paciente es obligatorio")
    @Size(max = 30, message = "El código del paciente no es válido")
    private String pacienteCodigo;

    @NotBlank(message = "El DNI del veterinario es obligatorio")
    @Size(max = 15, message = "El DNI del veterinario no es válido")
    private String veterinarioDni;

    @Min(value = 5, message = "La duración mínima es de 5 minutos")
    @Max(value = 240, message = "La duración máxima es de 240 minutos")
    private Integer duracionMinutos;

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

    public String getPacienteCodigo() {
        return pacienteCodigo;
    }

    public void setPacienteCodigo(String pacienteCodigo) {
        this.pacienteCodigo = pacienteCodigo;
    }

    public String getVeterinarioDni() {
        return veterinarioDni;
    }

    public void setVeterinarioDni(String veterinarioDni) {
        this.veterinarioDni = veterinarioDni;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }
}