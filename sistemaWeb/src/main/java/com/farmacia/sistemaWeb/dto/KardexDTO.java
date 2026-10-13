package com.farmacia.sistemaWeb.dto;

import com.farmacia.sistemaWeb.entity.Kardex;
import com.farmacia.sistemaWeb.entity.Lote;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Movimiento del kardex con usuario, lote y documento de origen. */
public class KardexDTO {
    private Long id;
    private LocalDateTime fechaHora;
    private String tipoOperacion;
    private String detalle;
    /** Cantidad sin signo (como antes) */
    private int cantidad;
    /** Positiva en entradas, negativa en salidas */
    private int cantidadConSigno;
    /** ENTRADA o SALIDA */
    private String movimiento;
    private int saldoResultante;
    private Long usuarioId;
    private String usuarioNombre;
    private Long loteId;
    private String loteCodigo;
    private LocalDate loteVencimiento;
    private String documentoTipo;
    private String documentoId;

    public static KardexDTO from(Kardex k) {
        KardexDTO d = new KardexDTO();
        d.id = k.getId();
        d.fechaHora = k.getFechaHora();
        d.tipoOperacion = k.getTipoOperacion() != null ? k.getTipoOperacion().name() : null;
        d.detalle = k.getDetalle();
        d.cantidad = Math.abs(k.getCantidad());
        boolean entrada = k.esEntrada();
        d.movimiento = entrada ? "ENTRADA" : "SALIDA";
        d.cantidadConSigno = entrada ? d.cantidad : -d.cantidad;
        d.saldoResultante = k.getSaldoResultante();
        if (k.getUsuario() != null) {
            d.usuarioId = k.getUsuario().getId();
            String nombre = k.getUsuario().getNombreCompleto();
            d.usuarioNombre = (nombre == null || nombre.isBlank()) ? k.getUsuario().getEmail() : nombre.trim();
        }
        Lote lote = k.getLote();
        if (lote != null) {
            d.loteId = lote.getId();
            d.loteCodigo = lote.getCodigoLote();
            d.loteVencimiento = lote.isSinVencimiento() ? null : lote.getFechaVencimiento();
        }
        d.documentoTipo = k.getDocumentoTipo();
        d.documentoId = k.getDocumentoId();
        return d;
    }

    public Long getId() { return id; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getTipoOperacion() { return tipoOperacion; }
    public String getDetalle() { return detalle; }
    public int getCantidad() { return cantidad; }
    public int getCantidadConSigno() { return cantidadConSigno; }
    public String getMovimiento() { return movimiento; }
    public int getSaldoResultante() { return saldoResultante; }
    public Long getUsuarioId() { return usuarioId; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public Long getLoteId() { return loteId; }
    public String getLoteCodigo() { return loteCodigo; }
    public LocalDate getLoteVencimiento() { return loteVencimiento; }
    public String getDocumentoTipo() { return documentoTipo; }
    public String getDocumentoId() { return documentoId; }
}
