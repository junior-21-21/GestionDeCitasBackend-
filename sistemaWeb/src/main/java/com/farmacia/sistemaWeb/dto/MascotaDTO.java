package com.farmacia.sistemaWeb.dto;

public class MascotaDTO {
    private String nombre;
    private String especie;
    private String raza;
    private int edad;
    private Long clienteId;

    // Getters y setters
    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecie() { return especie; }

    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaza() { return raza; }

    public void setRaza(String raza) { this.raza = raza; }

    public int getEdad() { return edad; }

    public void setEdad(int edad) { this.edad = edad; }

    public Long getClienteId() { return clienteId; }

    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
}
