package com.farmacia.sistemaWeb.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer stockMinimo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoProducto tipo;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Categoria categoria;


    // ── Clase, reglas y unidad (columnas nulas: ddl-auto=update las agrega a tablas con datos; la migración las llena) ──

    @Convert(converter = EnumTextoConverters.Clase.class)
    @Column(length = 30)
    private ClaseProducto clase;

    @Convert(converter = EnumTextoConverters.Linea.class)
    @Column(length = 20)
    private ClaseProducto.LineaNegocio lineaNegocio;

    @Column
    private Boolean requiereReceta;

    @Column
    private Boolean controlado;

    @Column
    private Boolean refrigerado;

    /** Lote y vencimiento obligatorios en cada ingreso. */
    @Column
    private Boolean requiereLote;

    @Convert(converter = EnumTextoConverters.Unidad.class)
    @Column(length = 20)
    private UnidadBase unidadBase;

    /** Costo promedio por unidad base con 6 decimales (precioCompra es el mismo valor redondeado a 2). */
    @Column(precision = 18, scale = 6)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    private BigDecimal costoUnitario;

    /** Presentaciones de venta y compra (factor = unidades base que contiene cada una). */
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    @OrderBy("factor ASC, id ASC")
    private java.util.List<Presentacion> presentaciones; // null en un JSON sin la lista = "no tocar presentaciones"

    // Solo para el alta: stock inicial que se registra como lote + movimiento de ingreso (no son columnas)
    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private Integer stockInicial;

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String loteInicial;

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private java.time.LocalDate vencimientoInicial;

    public enum TipoProducto {
        INSUMO_MEDICO,
        VENTA_PUBLICO,
        SERVICIO
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public TipoProducto getTipo() {
        return tipo;
    }

    public void setTipo(TipoProducto tipo) {
        this.tipo = tipo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Integer getStockInicial() {
        return stockInicial;
    }

    public void setStockInicial(Integer stockInicial) {
        this.stockInicial = stockInicial;
    }

    public String getLoteInicial() {
        return loteInicial;
    }

    public void setLoteInicial(String loteInicial) {
        this.loteInicial = loteInicial;
    }

    public java.time.LocalDate getVencimientoInicial() {
        return vencimientoInicial;
    }

    public void setVencimientoInicial(java.time.LocalDate vencimientoInicial) {
        this.vencimientoInicial = vencimientoInicial;
    }
    public ClaseProducto getClase() {
        return clase;
    }

    public void setClase(ClaseProducto clase) {
        this.clase = clase;
    }

    public ClaseProducto.LineaNegocio getLineaNegocio() {
        return lineaNegocio;
    }

    public void setLineaNegocio(ClaseProducto.LineaNegocio lineaNegocio) {
        this.lineaNegocio = lineaNegocio;
    }

    public Boolean getRequiereReceta() {
        return requiereReceta;
    }

    public void setRequiereReceta(Boolean requiereReceta) {
        this.requiereReceta = requiereReceta;
    }

    public Boolean getControlado() {
        return controlado;
    }

    public void setControlado(Boolean controlado) {
        this.controlado = controlado;
    }

    public Boolean getRefrigerado() {
        return refrigerado;
    }

    public void setRefrigerado(Boolean refrigerado) {
        this.refrigerado = refrigerado;
    }

    public Boolean getRequiereLote() {
        return requiereLote;
    }

    public void setRequiereLote(Boolean requiereLote) {
        this.requiereLote = requiereLote;
    }

    public UnidadBase getUnidadBase() {
        return unidadBase;
    }

    public void setUnidadBase(UnidadBase unidadBase) {
        this.unidadBase = unidadBase;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public java.util.List<Presentacion> getPresentaciones() {
        return presentaciones;
    }

    public void setPresentaciones(java.util.List<Presentacion> presentaciones) {
        this.presentaciones = presentaciones;
    }

    /** Etiqueta de la clase para pantallas (solo lectura). */
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    public String getClaseEtiqueta() {
        return clase != null ? clase.getEtiqueta() : null;
    }

    /** Plural de la unidad base para pantallas, p. ej. "tabletas" (solo lectura). */
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    public String getUnidadEtiqueta() {
        return (unidadBase != null ? unidadBase : UnidadBase.UNIDAD).getPlural();
    }

    public boolean exigeLote() {
        return Boolean.TRUE.equals(requiereLote);
    }
}
