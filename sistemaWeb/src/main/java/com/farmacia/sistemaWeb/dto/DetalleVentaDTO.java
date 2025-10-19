package com.farmacia.sistemaWeb.dto;

public class DetalleVentaDTO {
    private Long medicamentoId;
    private int cantidad;

    // Getters y Setters
    public Long getMedicamentoId() { return medicamentoId; }
    public void setMedicamentoId(Long medicamentoId) { this.medicamentoId = medicamentoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
