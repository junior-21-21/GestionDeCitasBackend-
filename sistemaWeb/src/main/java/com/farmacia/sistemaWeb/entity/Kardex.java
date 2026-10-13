package com.farmacia.sistemaWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kardex")
public class Kardex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Producto producto;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    // Se guarda como texto con un converter (no @Enumerated) para que Hibernate no genere un CHECK con la
    // lista de valores: así se pueden agregar tipos nuevos (VENCIMIENTO, ANULACION) sin romper la tabla.
    @Convert(converter = TipoOperacionConverter.class)
    @Column(nullable = false, length = 30)
    private TipoOperacion tipoOperacion;

    @Column(length = 255)
    private String detalle;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private int saldoResultante;

    /** ENTRADA o SALIDA. Nulo en movimientos antiguos: se deduce del tipo (solo INGRESO era entrada). */
    @Column(length = 10)
    private String movimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Lote lote;

    /** Documento de origen: COMPRA, VENTA, CONSULTA, VACUNA, AJUSTE, INICIAL, CONCILIACION. */
    @Column(length = 20)
    private String documentoTipo;

    @Column(length = 50)
    private String documentoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    // Solo se expone quién registró el movimiento, no el estado de su cuenta
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "intentosFallidos", "cuentaBloqueada", "habilitada", "fechaBloqueo"})
    private Usuario usuario;

    public enum TipoOperacion {
        INGRESO,
        VENTA,
        CONSUMO_INTERNO,
        AJUSTE,
        VENCIMIENTO,
        ANULACION
    }

    public boolean esEntrada() {
        if (movimiento != null) return "ENTRADA".equals(movimiento);
        return tipoOperacion == TipoOperacion.INGRESO;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public TipoOperacion getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(TipoOperacion tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getSaldoResultante() {
        return saldoResultante;
    }

    public void setSaldoResultante(int saldoResultante) {
        this.saldoResultante = saldoResultante;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getMovimiento() {
        return movimiento;
    }

    public void setMovimiento(String movimiento) {
        this.movimiento = movimiento;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public String getDocumentoTipo() {
        return documentoTipo;
    }

    public void setDocumentoTipo(String documentoTipo) {
        this.documentoTipo = documentoTipo;
    }

    public String getDocumentoId() {
        return documentoId;
    }

    public void setDocumentoId(String documentoId) {
        this.documentoId = documentoId;
    }
}
