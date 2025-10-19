package com.farmacia.sistemaWeb.dto;

import java.util.List;

public class VentaDTO {
    private Long clienteId;
    private List<DetalleVentaDTO> detalles;

    // Getters y Setters
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public List<DetalleVentaDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVentaDTO> detalles) { this.detalles = detalles; }
}
