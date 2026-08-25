package com.farmacia.sistemaWeb.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class PacienteDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "La raza es obligatoria")
    private Long razaId;

    /** Fecha de nacimiento — reemplaza el antiguo campo 'edad' (3FN) */
    @PastOrPresent(message = "La fecha de nacimiento no puede ser una fecha futura")
    private LocalDate fechaNacimiento;

    @Positive(message = "El peso debe ser mayor a cero")
    private Double peso;

    private String genero;

    @NotBlank(message = "El DNI del cliente es obligatorio")
    private String clienteDni;

    private String fotoUrl;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Long getRazaId() { return razaId; }
    public void setRazaId(Long razaId) { this.razaId = razaId; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getClienteDni() { return clienteDni; }
    public void setClienteDni(String clienteDni) { this.clienteDni = clienteDni; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
