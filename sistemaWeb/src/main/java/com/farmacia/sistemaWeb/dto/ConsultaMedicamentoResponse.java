package com.farmacia.sistemaWeb.dto;

public class ConsultaMedicamentoResponse {
    private Long consultaId;
    private Long medicamentoId;
    private int cantidad;
    private String nombreMedicamento;
    private String descripcionMedicamento;
    private double precioMedicamento;

    // Constructor
    public ConsultaMedicamentoResponse(Long consultaId, Long medicamentoId, int cantidad,
                                       String nombreMedicamento, String descripcionMedicamento, double precioMedicamento) {
        this.consultaId = consultaId;
        this.medicamentoId = medicamentoId;
        this.cantidad = cantidad;
        this.nombreMedicamento = nombreMedicamento;
        this.descripcionMedicamento = descripcionMedicamento;
        this.precioMedicamento = precioMedicamento;
    }

    // Getters y setters
    public Long getConsultaId() { return consultaId; }
    public void setConsultaId(Long consultaId) { this.consultaId = consultaId; }

    public Long getMedicamentoId() { return medicamentoId; }
    public void setMedicamentoId(Long medicamentoId) { this.medicamentoId = medicamentoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getNombreMedicamento() { return nombreMedicamento; }
    public void setNombreMedicamento(String nombreMedicamento) { this.nombreMedicamento = nombreMedicamento; }

    public String getDescripcionMedicamento() { return descripcionMedicamento; }
    public void setDescripcionMedicamento(String descripcionMedicamento) { this.descripcionMedicamento = descripcionMedicamento; }

    public double getPrecioMedicamento() { return precioMedicamento; }
    public void setPrecioMedicamento(double precioMedicamento) { this.precioMedicamento = precioMedicamento; }
}
