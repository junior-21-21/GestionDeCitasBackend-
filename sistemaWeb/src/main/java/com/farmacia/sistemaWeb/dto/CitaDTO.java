package com.farmacia.sistemaWeb.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaDTO {
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private String pacienteCodigo;
    private String veterinarioDni;
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