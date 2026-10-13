package com.farmacia.sistemaWeb.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Texto plano con converter: sin CHECK de valores en SQL Server (los roles nuevos no rompen los INSERT)
    @Convert(converter = EnumTextoConverters.NombreRol.class)
    @Column(length = 20)
    private NombreRol nombre;

    public enum NombreRol {
        ADMIN, RECEPCIONISTA, VETERINARIO, CLIENTE,
        /** Encargado de farmacia y almacén */
        ENCARGADO_ALMACEN,
        /** Baño y corte */
        GROOMER
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NombreRol getNombre() {
        return nombre;
    }

    public void setNombre(NombreRol nombre) {
        this.nombre = nombre;
    }
}
