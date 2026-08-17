package com.farmacia.sistemaWeb.dto;

import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDTO {

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 11, message = "El documento debe tener entre 8 y 11 caracteres")
    private String dni;

    private String telefono;

    // ── Dirección atómica (1FN) ──
    private String calle;
    private String numero;
    private String distrito;
    private String provincia;

    // ── Teléfonos adicionales (1FN) ──
    private List<TelefonoDTO> telefonos = new ArrayList<>();

    // ── DTO anidado para teléfonos ──
    public static class TelefonoDTO {
        private String numero;
        private String tipo; // CELULAR, CASA, TRABAJO

        public String getNumero() { return numero; }
        public void setNumero(String numero) { this.numero = numero; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
    }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    public List<TelefonoDTO> getTelefonos() { return telefonos; }
    public void setTelefonos(List<TelefonoDTO> telefonos) { this.telefonos = telefonos; }
}