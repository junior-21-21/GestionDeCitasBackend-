package com.farmacia.sistemaWeb.dto;

public class MascotaDTO {

    @jakarta.validation.constraints.NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @jakarta.validation.constraints.NotBlank(message = "La especie es obligatoria")
    private String especie;

    @jakarta.validation.constraints.NotBlank(message = "La raza es obligatoria")
    private String raza;

    @jakarta.validation.constraints.Min(value = 0, message = "La edad no puede ser negativa")
    private int edad;

    @jakarta.validation.constraints.NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
