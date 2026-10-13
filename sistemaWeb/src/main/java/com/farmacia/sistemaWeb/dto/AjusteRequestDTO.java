package com.farmacia.sistemaWeb.dto;

import java.time.LocalDate;

/** Petición de ajuste de inventario. */
public class AjusteRequestDTO {
    private Long productoId;
    /** ENTRADA o SALIDA */
    private String tipo;
    private Integer cantidad;
    /** Salida: lote del que sale (obligatorio). Entrada: lote existente al que entra (opcional). */
    private Long loteId;
    /** Entrada a lote nuevo: código (opcional, se genera si falta) y vencimiento. */
    private String codigoLote;
    private LocalDate fechaVencimiento;
    /** MERMA, ROTURA, VENCIDO, CONTEO_FISICO, DONACION u OTRO */
    private String motivo;
    private String observacion;

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public String getCodigoLote() { return codigoLote; }
    public void setCodigoLote(String codigoLote) { this.codigoLote = codigoLote; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
