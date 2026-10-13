package com.farmacia.sistemaWeb.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class PacienteDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotNull(message = "La raza es obligatoria")
    private Long razaId;

    /** Fecha de nacimiento — reemplaza el antiguo campo 'edad' (3FN) */
    @PastOrPresent(message = "La fecha de nacimiento no puede ser una fecha futura")
    private LocalDate fechaNacimiento;

    @Positive(message = "El peso debe ser mayor a cero")
    @Max(value = 1000, message = "El peso no puede superar los 1000 kg")
    private Double peso;

    @Size(max = 20, message = "El género no puede superar los 20 caracteres")
    private String genero;

    @NotBlank(message = "El DNI del cliente es obligatorio")
    @Pattern(regexp = "^(\\d{8}|\\d{11})$", message = "El DNI debe tener 8 dígitos o el RUC 11 dígitos")
    private String clienteDni;

    private String fotoUrl;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Long getRazaId() { return razaId; }
    public void setRazaId(Long razaId) { this.razaId = razaId; }
    @AssertTrue(message = "La fecha de nacimiento no puede ser anterior a 1980")
    private boolean isFechaNacimientoRazonable() {
        return fechaNacimiento == null || !fechaNacimiento.isBefore(LocalDate.of(1980, 1, 1));
    }

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
