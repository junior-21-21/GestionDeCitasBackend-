package com.farmacia.sistemaWeb.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "consulta_medicamentos")
public class ConsultaMedicamento {

    @EmbeddedId
    private ConsultaMedicamentoPK id = new ConsultaMedicamentoPK();

    @ManyToOne
    @MapsId("consultaId")
    @JoinColumn(name = "consulta_id")
    @JsonIgnore
    @JsonBackReference("medicamento-consulta")
    private Consulta consulta;

    @ManyToOne
    @MapsId("medicamentoId")
    @JoinColumn(name = "medicamento_id")
    @JsonBackReference("consulta-medicamento")
    private Medicamento medicamento;

    private int cantidad;

    // Getters y setters
    public ConsultaMedicamentoPK getId() { return id; }
    public void setId(ConsultaMedicamentoPK id) { this.id = id; }

    public Consulta getConsulta() { return consulta; }
    public void setConsulta(Consulta consulta) { this.consulta = consulta; }

    public Medicamento getMedicamento() { return medicamento; }
    public void setMedicamento(Medicamento medicamento) { this.medicamento = medicamento; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
