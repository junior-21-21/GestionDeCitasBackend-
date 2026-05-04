package com.farmacia.sistemaWeb.dto;

import java.time.LocalDateTime;

public class MovimientoInventarioResponseDTO {
    private Long id;
    private String tipoMovimiento;
    private int cantidad;
    private LocalDateTime fechaHora;
    private String motivoAjuste;
    private Long referenciaId;

    private ProductoInfo producto;
    private LoteInfo lote;
    private UsuarioInfo usuario;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getMotivoAjuste() {
        return motivoAjuste;
    }

    public void setMotivoAjuste(String motivoAjuste) {
        this.motivoAjuste = motivoAjuste;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public ProductoInfo getProducto() {
        return producto;
    }

    public void setProducto(ProductoInfo producto) {
        this.producto = producto;
    }

    public LoteInfo getLote() {
        return lote;
    }

    public void setLote(LoteInfo lote) {
        this.lote = lote;
    }

    public UsuarioInfo getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioInfo usuario) {
        this.usuario = usuario;
    }

    public static class ProductoInfo {
        private String codigoBarras;
        private String nombre;
        private boolean isControlado;

        public String getCodigoBarras() {
            return codigoBarras;
        }

        public void setCodigoBarras(String codigoBarras) {
            this.codigoBarras = codigoBarras;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public boolean isIsControlado() {
            return isControlado;
        }

        public void setIsControlado(boolean isControlado) {
            this.isControlado = isControlado;
        }
    }

    public static class LoteInfo {
        private Long id;
        private String numeroLote;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNumeroLote() {
            return numeroLote;
        }

        public void setNumeroLote(String numeroLote) {
            this.numeroLote = numeroLote;
        }
    }

    public static class UsuarioInfo {
        private Long id;
        private String nombres;
        private String apellidos;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNombres() {
            return nombres;
        }

        public void setNombres(String nombres) {
            this.nombres = nombres;
        }

        public String getApellidos() {
            return apellidos;
        }

        public void setApellidos(String apellidos) {
            this.apellidos = apellidos;
        }
    }
}
