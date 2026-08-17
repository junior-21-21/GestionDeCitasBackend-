package com.farmacia.sistemaWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @Column(length = 20, nullable = false) // Can be DNI (8) or RUC (11)
    private String dni;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 10)
    private TipoDocumento tipoDocumento;

    @Column(name = "razon_social")
    private String razonSocial;

    private String nombres;
    private String apellidos;

    /** Teléfono principal — se mantiene por retrocompatibilidad */
    private String telefono;

    // ── Dirección atómica (1FN) ──
    @Column(length = 150)
    private String calle;

    @Column(length = 20)
    private String numero;

    @Column(length = 80)
    private String distrito;

    @Column(length = 80)
    private String provincia;

    @Column(unique = true)
    private String email;

    @Column(name = "puntos_fidelidad")
    private Integer puntosFidelidad;

    /** Teléfonos adicionales — 1FN: cada teléfono en su propio registro */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("cliente")
    private List<TelefonoCliente> telefonos = new ArrayList<>();

    // ── Enum ──

    public enum TipoDocumento {
        DNI, RUC
    }

    // ── Método de conveniencia: dirección completa concatenada ──
    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder();
        if (calle != null && !calle.isBlank()) sb.append(calle);
        if (numero != null && !numero.isBlank()) sb.append(" ").append(numero);
        if (distrito != null && !distrito.isBlank()) sb.append(", ").append(distrito);
        if (provincia != null && !provincia.isBlank()) sb.append(", ").append(provincia);
        return sb.toString().trim();
    }

    // Getters y Setters
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

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

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getPuntosFidelidad() {
        return puntosFidelidad == null ? 0 : puntosFidelidad;
    }
    public void setPuntosFidelidad(Integer puntosFidelidad) { this.puntosFidelidad = puntosFidelidad; }

    public List<TelefonoCliente> getTelefonos() { return telefonos; }
    public void setTelefonos(List<TelefonoCliente> telefonos) { this.telefonos = telefonos; }
}