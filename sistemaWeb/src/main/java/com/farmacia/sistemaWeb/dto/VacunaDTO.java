package com.farmacia.sistemaWeb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class VacunaDTO {

    @NotBlank(message = "El nombre de la vacuna es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombre;

    private String fabricante;
    private String tipo;        // OBLIGATORIA, OPCIONAL, REFUERZO
    private String descripcion;
    private Integer periodicidadMeses;

    /** IDs de las especies a las que aplica esta vacuna */
    private List<Long> especieIds;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getPeriodicidadMeses() { return periodicidadMeses; }
    public void setPeriodicidadMeses(Integer periodicidadMeses) { this.periodicidadMeses = periodicidadMeses; }

    public List<Long> getEspecieIds() { return especieIds; }
    public void setEspecieIds(List<Long> especieIds) { this.especieIds = especieIds; }
}
