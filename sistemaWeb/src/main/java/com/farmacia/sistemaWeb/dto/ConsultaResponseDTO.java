package com.farmacia.sistemaWeb.dto;

public class ConsultaResponseDTO {
    private String codigoConsulta;
    private String motivo;
    private String diagnostico;
    private String tratamiento;
    private String fecha;
    private String nombrePaciente;
    private String nombreVeterinario;

    public String getCodigoConsulta() {
        return codigoConsulta;
    }

    public void setCodigoConsulta(String codigoConsulta) {
        this.codigoConsulta = codigoConsulta;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    public String getNombreVeterinario() {
        return nombreVeterinario;
    }

    public void setNombreVeterinario(String nombreVeterinario) {
        this.nombreVeterinario = nombreVeterinario;
    }

    private String estadoIngreso;
    private String estadoSalida;
    private boolean requiereInternamiento;
    private String motivoInternamiento;

    public String getEstadoIngreso() { return estadoIngreso; }
    public void setEstadoIngreso(String estadoIngreso) { this.estadoIngreso = estadoIngreso; }
    public String getEstadoSalida() { return estadoSalida; }
    public void setEstadoSalida(String estadoSalida) { this.estadoSalida = estadoSalida; }
    public boolean isRequiereInternamiento() { return requiereInternamiento; }
    public void setRequiereInternamiento(boolean requiereInternamiento) { this.requiereInternamiento = requiereInternamiento; }
    public String getMotivoInternamiento() { return motivoInternamiento; }
    public void setMotivoInternamiento(String motivoInternamiento) { this.motivoInternamiento = motivoInternamiento; }
}
