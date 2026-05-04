package com.farmacia.sistemaWeb.dto;

public class PacienteDTO {

    @jakarta.validation.constraints.NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String especie;

    private String raza;

    @jakarta.validation.constraints.Min(value = 0, message = "La edad no puede ser negativa")
    private int edad;

    private Double peso;

    @jakarta.validation.constraints.NotBlank(message = "El DNI del cliente es obligatorio")
    private String clienteDni;

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

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getClienteDni() {
        return clienteDni;
    }

    public void setClienteDni(String clienteDni) {
        this.clienteDni = clienteDni;
    }
}
