package com.farmacia.sistemaWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @Column(name = "codigo_paciente", length = 30, nullable = false)
    private String codigoPaciente;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "raza_id", nullable = false)
    @JsonIgnoreProperties({"especie"})
    private Raza raza;

    /** Fecha de nacimiento — reemplaza el antiguo campo 'edad' (3FN: atributo derivado) */
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private Double peso;

    @Column(length = 20)
    private String genero;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_dni", referencedColumnName = "dni", nullable = false)
    private Cliente cliente;

    // ── Método derivado: calcula edad en años desde fechaNacimiento ──
    public Integer getEdadCalculada() {
        if (fechaNacimiento == null) return null;
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    // Getters y Setters
    public String getCodigoPaciente() { return codigoPaciente; }
    public void setCodigoPaciente(String codigoPaciente) { this.codigoPaciente = codigoPaciente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Raza getRaza() { return raza; }
    public void setRaza(Raza raza) { this.raza = raza; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
