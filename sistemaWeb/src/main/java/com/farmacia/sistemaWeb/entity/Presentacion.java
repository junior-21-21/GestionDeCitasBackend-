package com.farmacia.sistemaWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Forma en que se vende o se compra un producto: "Caja x100", "Blíster x10", "Tableta", "Bolsa 15 kg", "Kilo"...
 * El factor indica cuántas unidades base contiene. El stock siempre se guarda en unidades base.
 * El código de barras es único entre todas las presentaciones (se valida en el servicio y, en SQL Server,
 * con un índice único filtrado que admite varios NULL).
 */
@Entity
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "presentaciones", indexes = {
        @Index(name = "ix_presentaciones_codigo_barras", columnList = "codigo_barras"),
        @Index(name = "ix_presentaciones_producto", columnList = "producto_id")
})
public class Presentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnore
    private Producto producto;

    @Column(nullable = false, length = 60)
    private String nombre;

    /** Unidades base que contiene (Caja x100 = 100; Kilo de un producto en gramos = 1000). */
    @Column(nullable = false)
    private Integer factor;

    @Column(name = "codigo_barras", length = 50)
    private String codigoBarras;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(nullable = false)
    private boolean vendible = true;

    @Column(nullable = false)
    private boolean comprable = true;

    @Column(nullable = false)
    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getProductoId() { return producto != null ? producto.getId() : null; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getFactor() { return factor; }
    public void setFactor(Integer factor) { this.factor = factor; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public boolean isVendible() { return vendible; }
    public void setVendible(boolean vendible) { this.vendible = vendible; }

    public boolean isComprable() { return comprable; }
    public void setComprable(boolean comprable) { this.comprable = comprable; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /** La presentación base (factor 1) es la unidad mínima: la que se usa si una venta no indica presentación. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isBase() { return factor != null && factor == 1; }
}
