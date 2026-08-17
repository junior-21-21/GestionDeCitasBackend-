package com.farmacia.sistemaWeb.dto;

import java.time.LocalDate;

/**
 * DTO de respuesta para el registro de vacunación con datos aplanados.
 */
public class RegistroVacunaResponseDTO {

    private Long id;
    private String vacunaNombre;
    private String vacunaTipo;
    private String pacienteNombre;
    private String pacienteCodigo;
    private String veterinarioNombre;
    private String veterinarioDni;
    private LocalDate fechaAplicacion;
    private LocalDate proximaDosis;
    /** Datos del lote real del inventario */
    private Long loteId;
    private String loteNumero;
    private String loteFechaVencimiento;
    private String observaciones;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVacunaNombre() { return vacunaNombre; }
    public void setVacunaNombre(String vacunaNombre) { this.vacunaNombre = vacunaNombre; }

    public String getVacunaTipo() { return vacunaTipo; }
    public void setVacunaTipo(String vacunaTipo) { this.vacunaTipo = vacunaTipo; }

    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }

    public String getPacienteCodigo() { return pacienteCodigo; }
    public void setPacienteCodigo(String pacienteCodigo) { this.pacienteCodigo = pacienteCodigo; }

    public String getVeterinarioNombre() { return veterinarioNombre; }
    public void setVeterinarioNombre(String veterinarioNombre) { this.veterinarioNombre = veterinarioNombre; }

    public String getVeterinarioDni() { return veterinarioDni; }
    public void setVeterinarioDni(String veterinarioDni) { this.veterinarioDni = veterinarioDni; }

    public LocalDate getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(LocalDate fechaAplicacion) { this.fechaAplicacion = fechaAplicacion; }

    public LocalDate getProximaDosis() { return proximaDosis; }
    public void setProximaDosis(LocalDate proximaDosis) { this.proximaDosis = proximaDosis; }

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }

    public String getLoteNumero() { return loteNumero; }
    public void setLoteNumero(String loteNumero) { this.loteNumero = loteNumero; }

    public String getLoteFechaVencimiento() { return loteFechaVencimiento; }
    public void setLoteFechaVencimiento(String loteFechaVencimiento) { this.loteFechaVencimiento = loteFechaVencimiento; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
