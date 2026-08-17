package com.farmacia.sistemaWeb.dto;

import jakarta.validation.constraints.*;

public class VeterinarioDTO {

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    private String celular;
    private String correo;

    @NotNull(message = "La especialidad es obligatoria")
    private Long especialidadId;

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public Long getEspecialidadId() { return especialidadId; }
    public void setEspecialidadId(Long especialidadId) { this.especialidadId = especialidadId; }
}