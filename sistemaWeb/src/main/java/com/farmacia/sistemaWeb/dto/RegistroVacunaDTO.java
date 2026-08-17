package com.farmacia.sistemaWeb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class RegistroVacunaDTO {

    @NotNull(message = "La vacuna es obligatoria")
    private Long vacunaId;

    @NotBlank(message = "El código del paciente es obligatorio")
    private String pacienteCodigo;

    @NotBlank(message = "El DNI del veterinario es obligatorio")
    private String veterinarioDni;

    @NotNull(message = "La fecha de aplicación es obligatoria")
    private LocalDate fechaAplicacion;

    /** ID del lote real del inventario que se usó en esta vacunación */
    private Long loteId;

    private LocalDate proximaDosis;
    private String observaciones;


    public Long getVacunaId() { return vacunaId; }
    public void setVacunaId(Long vacunaId) { this.vacunaId = vacunaId; }

    public String getPacienteCodigo() { return pacienteCodigo; }
    public void setPacienteCodigo(String pacienteCodigo) { this.pacienteCodigo = pacienteCodigo; }

    public String getVeterinarioDni() { return veterinarioDni; }
    public void setVeterinarioDni(String veterinarioDni) { this.veterinarioDni = veterinarioDni; }

    public LocalDate getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(LocalDate fechaAplicacion) { this.fechaAplicacion = fechaAplicacion; }

    public LocalDate getProximaDosis() { return proximaDosis; }
    public void setProximaDosis(LocalDate proximaDosis) { this.proximaDosis = proximaDosis; }

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
