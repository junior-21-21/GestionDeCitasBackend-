package com.farmacia.sistemaWeb.dto;

import com.farmacia.sistemaWeb.entity.Lote;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Lote para pantallas y alertas: incluye el estado de vencimiento ya calculado. */
public class LoteDTO {
    private Long id;
    private String codigoLote;
    private LocalDate fechaVencimiento; // null = sin vencimiento
    private boolean sinVencimiento;
    private Integer cantidadInicial;
    private Integer stockActual;
    private LocalDateTime fechaIngreso;
    /** VENCIDO, POR_VENCER (30 días), VIGENTE o AGOTADO */
    private String estado;
    private Long diasParaVencer;
    private Long productoId;
    private String productoCodigo;
    private String productoNombre;

    public static final int DIAS_ALERTA = 30;

    public static LoteDTO from(Lote l, boolean conProducto) {
        LoteDTO d = new LoteDTO();
        d.id = l.getId();
        d.codigoLote = l.getCodigoLote();
        d.sinVencimiento = l.isSinVencimiento();
        d.fechaVencimiento = d.sinVencimiento ? null : l.getFechaVencimiento();
        d.cantidadInicial = l.getCantidadInicial();
        d.stockActual = l.getStockActual();
        d.fechaIngreso = l.getFechaIngreso();
        LocalDate hoy = LocalDate.now();
        if (!d.sinVencimiento && d.fechaVencimiento != null) {
            d.diasParaVencer = ChronoUnit.DAYS.between(hoy, d.fechaVencimiento);
        }
        int stock = l.getStockActual() == null ? 0 : l.getStockActual();
        if (stock <= 0) {
            d.estado = "AGOTADO";
        } else if (l.isVencido()) {
            d.estado = "VENCIDO";
        } else if (d.diasParaVencer != null && d.diasParaVencer <= DIAS_ALERTA) {
            d.estado = "POR_VENCER";
        } else {
            d.estado = "VIGENTE";
        }
        if (conProducto && l.getProducto() != null) {
            d.productoId = l.getProducto().getId();
            d.productoCodigo = l.getProducto().getCodigo();
            d.productoNombre = l.getProducto().getNombre();
        }
        return d;
    }

    public Long getId() { return id; }
    public String getCodigoLote() { return codigoLote; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public boolean isSinVencimiento() { return sinVencimiento; }
    public Integer getCantidadInicial() { return cantidadInicial; }
    public Integer getStockActual() { return stockActual; }
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public String getEstado() { return estado; }
    public Long getDiasParaVencer() { return diasParaVencer; }
    public Long getProductoId() { return productoId; }
    public String getProductoCodigo() { return productoCodigo; }
    public String getProductoNombre() { return productoNombre; }
}
