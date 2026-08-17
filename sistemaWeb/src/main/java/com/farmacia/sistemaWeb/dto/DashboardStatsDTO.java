package com.farmacia.sistemaWeb.dto;

import java.util.Map;

public class DashboardStatsDTO {
    private long citas;
    private long consultas;
    private long pacientes;
    private Map<String, Long> topEspecies;

    // Getters and Setters
    public long getCitas() { return citas; }
    public void setCitas(long citas) { this.citas = citas; }

    public long getConsultas() { return consultas; }
    public void setConsultas(long consultas) { this.consultas = consultas; }

    public long getPacientes() { return pacientes; }
    public void setPacientes(long pacientes) { this.pacientes = pacientes; }

    public Map<String, Long> getTopEspecies() { return topEspecies; }
    public void setTopEspecies(Map<String, Long> topEspecies) { this.topEspecies = topEspecies; }
}
