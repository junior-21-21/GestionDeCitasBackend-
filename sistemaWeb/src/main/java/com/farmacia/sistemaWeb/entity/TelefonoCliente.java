package com.farmacia.sistemaWeb.entity;

import jakarta.persistence.*;

/**
 * TelefonoCliente — Normalización 1FN: cada teléfono de un cliente
 * se almacena en un registro propio, eliminando multivaluados.
 */
@Entity
@Table(name = "telefonos_cliente")
public class TelefonoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private TipoTelefono tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_dni", referencedColumnName = "dni", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Cliente cliente;

    public enum TipoTelefono {
        CELULAR, CASA, TRABAJO
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public TipoTelefono getTipo() { return tipo; }
    public void setTipo(TipoTelefono tipo) { this.tipo = tipo; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
}
