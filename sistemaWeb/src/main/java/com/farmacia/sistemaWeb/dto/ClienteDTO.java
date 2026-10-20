package com.farmacia.sistemaWeb.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDTO {

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar los 100 caracteres")
    @Pattern(regexp = "^$|^\\p{L}[\\p{L} .'\\-]*$", message = "Los nombres solo pueden contener letras y espacios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    @Pattern(regexp = "^$|^\\p{L}[\\p{L} .'\\-]*$", message = "Los apellidos solo pueden contener letras y espacios")
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^(\\d{8}|\\d{11})$", message = "El documento debe ser un DNI de 8 dígitos o un RUC de 11 dígitos")
    private String dni;

    // Opcional: se acepta vacío porque el formulario puede enviar ""
    @Pattern(regexp = "^$|^\\d{7,9}$", message = "El teléfono debe tener entre 7 y 9 dígitos")
    private String telefono;

    // ── Dirección atómica (1FN) ──
    @Size(max = 150, message = "La calle no puede superar los 150 caracteres")
    private String calle;
    @Size(max = 20, message = "El número no puede superar los 20 caracteres")
    private String numero;
    @Size(max = 80, message = "El distrito no puede superar los 80 caracteres")
    private String distrito;
    @Size(max = 80, message = "La provincia no puede superar los 80 caracteres")
    private String provincia;

    // ── Teléfonos adicionales (1FN) ──
    @Valid
    @Size(max = 5, message = "Máximo 5 teléfonos adicionales")
    private List<TelefonoDTO> telefonos = new ArrayList<>();

    // ── DTO anidado para teléfonos ──
    public static class TelefonoDTO {
        @NotBlank(message = "El número de teléfono es obligatorio")
        @Pattern(regexp = "^\\d{7,9}$", message = "El teléfono debe tener entre 7 y 9 dígitos")
        private String numero;

        @NotBlank(message = "El tipo de teléfono es obligatorio")
        @Pattern(regexp = "^(CELULAR|CASA|TRABAJO)$", message = "Tipo de teléfono inválido (CELULAR, CASA o TRABAJO)")
        private String tipo;

        @AssertTrue(message = "El celular debe tener 9 dígitos y empezar con 9")
        private boolean isCelularValido() {
            return !"CELULAR".equals(tipo) || numero == null || numero.matches("^9\\d{8}$");
        }

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