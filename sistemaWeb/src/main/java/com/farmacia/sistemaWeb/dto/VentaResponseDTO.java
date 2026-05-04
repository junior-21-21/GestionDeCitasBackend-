package com.farmacia.sistemaWeb.dto;

import java.time.LocalDate;
import java.util.List;

public class VentaResponseDTO {
    private Long id;
    private String codigoVenta;
    private String tipoComprobante;
    private String serie;
    private String correlativo;
    private Double subtotal;
    private Double igv;
    private Double descuento;
    private Double total;
    private String metodoPago;
    private LocalDate fecha;
    private String recetaMedica;

    private ClienteInfo cliente;
    private List<DetalleVentaInfo> detalles;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoVenta() {
        return codigoVenta;
    }

    public void setCodigoVenta(String codigoVenta) {
        this.codigoVenta = codigoVenta;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getCorrelativo() {
        return correlativo;
    }

    public void setCorrelativo(String correlativo) {
        this.correlativo = correlativo;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getIgv() {
        return igv;
    }

    public void setIgv(Double igv) {
        this.igv = igv;
    }

    public Double getDescuento() {
        return descuento;
    }

    public void setDescuento(Double descuento) {
        this.descuento = descuento;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getRecetaMedica() {
        return recetaMedica;
    }

    public void setRecetaMedica(String recetaMedica) {
        this.recetaMedica = recetaMedica;
    }

    public ClienteInfo getCliente() {
        return cliente;
    }

    public void setCliente(ClienteInfo cliente) {
        this.cliente = cliente;
    }

    public List<DetalleVentaInfo> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaInfo> detalles) {
        this.detalles = detalles;
    }

    public static class ClienteInfo {
        private String dni;
        private String nombres;
        private String apellidos;
        private String razonSocial;
        private String ruc;

        public String getDni() {
            return dni;
        }

        public void setDni(String dni) {
            this.dni = dni;
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

        public String getRazonSocial() {
            return razonSocial;
        }

        public void setRazonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
        }

        public String getRuc() {
            return ruc;
        }

        public void setRuc(String ruc) {
            this.ruc = ruc;
        }
    }

    public static class DetalleVentaInfo {
        private String codigoDetalle;
        private int cantidad;
        private double precio;
        private String productoCodigoBarras;
        private String productoNombre;

        public String getCodigoDetalle() {
            return codigoDetalle;
        }

        public void setCodigoDetalle(String codigoDetalle) {
            this.codigoDetalle = codigoDetalle;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }

        public double getPrecio() {
            return precio;
        }

        public void setPrecio(double precio) {
            this.precio = precio;
        }

        public String getProductoCodigoBarras() {
            return productoCodigoBarras;
        }

        public void setProductoCodigoBarras(String productoCodigoBarras) {
            this.productoCodigoBarras = productoCodigoBarras;
        }

        public String getProductoNombre() {
            return productoNombre;
        }

        public void setProductoNombre(String productoNombre) {
            this.productoNombre = productoNombre;
        }
    }
}
