package com.farmacia.sistemaWeb.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ConsultaMedicamentoPK implements Serializable {
    private Long consultaId;
    private Long medicamentoId;

    public ConsultaMedicamentoPK() {}

    public ConsultaMedicamentoPK(Long consultaId, Long medicamentoId) {
        this.consultaId = consultaId;
        this.medicamentoId = medicamentoId;
    }

    public Long getConsultaId() { return consultaId; }
    public void setConsultaId(Long consultaId) { this.consultaId = consultaId; }

    public Long getMedicamentoId() { return medicamentoId; }
    public void setMedicamentoId(Long medicamentoId) { this.medicamentoId = medicamentoId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultaMedicamentoPK)) return false;
        ConsultaMedicamentoPK that = (ConsultaMedicamentoPK) o;
        return Objects.equals(consultaId, that.consultaId) &&
                Objects.equals(medicamentoId, that.medicamentoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consultaId, medicamentoId);
    }
}
