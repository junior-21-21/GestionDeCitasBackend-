package com.farmacia.sistemaWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cobro_consulta")
public class CobroConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false, length = 20)
    private String estado; // PAGADO, PENDIENTE

    @Column(name = "detalle_cargos", nullable = false, columnDefinition = "TEXT")
    private String detalleCargos; // JSON array of items: {descripcion, precio, cantidad, subtotal}

    @OneToOne
    @JoinColumn(name = "consulta_codigo", referencedColumnName = "codigo_consulta", nullable = false)
    @JsonIgnoreProperties({"cita", "paciente", "veterinario"})
    private Consulta consulta;

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = "PENDIENTE";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDetalleCargos() {
        return detalleCargos;
    }

    public void setDetalleCargos(String detalleCargos) {
        this.detalleCargos = detalleCargos;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }
}
