package com.farmacia.sistemaWeb.dto;

public class ConsultaMedicamentoDTO {
    private Long consultaId;
    private Long medicamentoId;
    private int cantidad;

    // Getters y Setters
    public Long getConsultaId() { return consultaId; }
    public void setConsultaId(Long consultaId) { this.consultaId = consultaId; }

    public Long getMedicamentoId() { return medicamentoId; }
    public void setMedicamentoId(Long medicamentoId) { this.medicamentoId = medicamentoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
