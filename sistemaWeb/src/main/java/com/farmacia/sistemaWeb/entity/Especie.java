package com.farmacia.sistemaWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "especies")
public class Especie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String nombre;

    @OneToMany(mappedBy = "especie", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("especie")
    private List<Raza> razas = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public List<Raza> getRazas() { return razas; }
    public void setRazas(List<Raza> razas) { this.razas = razas; }
}
