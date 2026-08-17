package com.farmacia.sistemaWeb.dto;

public class DashboardDTO {
    private long totalPacientes;
    private long totalClientes;
    private long totalVeterinarios;
    private long citasPendientesHoy;
    private long citasCompletadasHoy;

    public long getTotalPacientes() { return totalPacientes; }
    public void setTotalPacientes(long totalPacientes) { this.totalPacientes = totalPacientes; }

    public long getTotalClientes() { return totalClientes; }
    public void setTotalClientes(long totalClientes) { this.totalClientes = totalClientes; }

    public long getTotalVeterinarios() { return totalVeterinarios; }
    public void setTotalVeterinarios(long totalVeterinarios) { this.totalVeterinarios = totalVeterinarios; }

    public long getCitasPendientesHoy() { return citasPendientesHoy; }
    public void setCitasPendientesHoy(long citasPendientesHoy) { this.citasPendientesHoy = citasPendientesHoy; }

    public long getCitasCompletadasHoy() { return citasCompletadasHoy; }
    public void setCitasCompletadasHoy(long citasCompletadasHoy) { this.citasCompletadasHoy = citasCompletadasHoy; }
}
