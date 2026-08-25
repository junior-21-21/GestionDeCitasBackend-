package com.farmacia.sistemaWeb.dto;

public class PacienteResponseDTO {
    private String codigoPaciente;
    private String nombre;
    private String especie;
    private String raza;
    private Long especieId;
    private Long razaId;
    private String fechaNacimiento;
    private int edadCalculada;
    private Double peso;
    private String genero;
    private String clienteDni;
    private String clienteNombreCompleto;
    private String fotoUrl;

    public String getCodigoPaciente() { return codigoPaciente; }
    public void setCodigoConsulta(String codigoPaciente) { this.codigoPaciente = codigoPaciente; }
    public void setCodigoPaciente(String codigoPaciente) { this.codigoPaciente = codigoPaciente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public Long getEspecieId() { return especieId; }
    public void setEspecieId(Long especieId) { this.especieId = especieId; }
    public Long getRazaId() { return razaId; }
    public void setRazaId(Long razaId) { this.razaId = razaId; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public int getEdadCalculada() { return edadCalculada; }
    public void setEdadCalculada(int edadCalculada) { this.edadCalculada = edadCalculada; }
    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getClienteDni() { return clienteDni; }
    public void setClienteDni(String clienteDni) { this.clienteDni = clienteDni; }
    public String getClienteNombreCompleto() { return clienteNombreCompleto; }
    public void setClienteNombreCompleto(String clienteNombreCompleto) { this.clienteNombreCompleto = clienteNombreCompleto; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
