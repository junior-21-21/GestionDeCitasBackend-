package com.farmacia.sistemaWeb.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "asistencia_diaria")
public class AsistenciaDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_apertura", nullable = false)
    private LocalDateTime horaApertura;

    @Column(name = "hora_cierre")
    private LocalDateTime horaCierre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAsistencia estado;

    public enum EstadoAsistencia {
        ABIERTO,
        CERRADO
    }
}
