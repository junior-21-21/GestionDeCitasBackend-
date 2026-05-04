package com.farmacia.sistemaWeb.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "consulta_productos")
public class ConsultaProducto {

    @EmbeddedId
    private ConsultaProductoPK id = new ConsultaProductoPK();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("codigoConsulta")
    @JoinColumn(name = "codigo_consulta", referencedColumnName = "codigo_consulta")
    @JsonIgnore
    @JsonBackReference("medicamento-consulta")
    private Consulta consulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("codigoBarras")
    @JoinColumn(name = "codigo_barras", referencedColumnName = "codigo_barras")
    @JsonBackReference("consulta-medicamento")
    private Producto producto;

    private int cantidad;
    private String indicaciones;

    // Getters and setters
    public ConsultaProductoPK getId() {
        return id;
    }

    public void setId(ConsultaProductoPK id) {
        this.id = id;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }
}
