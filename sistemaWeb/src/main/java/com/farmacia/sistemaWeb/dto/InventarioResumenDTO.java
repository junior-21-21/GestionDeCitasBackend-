package com.farmacia.sistemaWeb.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Resumen de inventario para el panel: valor a costo promedio y alertas. */
public class InventarioResumenDTO {
    private BigDecimal valorInventario = BigDecimal.ZERO;
    private long unidades;
    private long productosActivos;
    private List<ProductoAlerta> stockBajo = new ArrayList<>();
    private List<LoteDTO> lotesPorVencer = new ArrayList<>();
    private List<LoteDTO> lotesVencidos = new ArrayList<>();
    private int diasAlerta = LoteDTO.DIAS_ALERTA;

    public static class ProductoAlerta {
        private Long id;
        private String codigo;
        private String nombre;
        private int stock;
        private int stockMinimo;

        public ProductoAlerta(Long id, String codigo, String nombre, int stock, int stockMinimo) {
            this.id = id;
            this.codigo = codigo;
            this.nombre = nombre;
            this.stock = stock;
            this.stockMinimo = stockMinimo;
        }

        public Long getId() { return id; }
        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public int getStock() { return stock; }
        public int getStockMinimo() { return stockMinimo; }
    }

    public BigDecimal getValorInventario() { return valorInventario; }
    public void setValorInventario(BigDecimal valorInventario) { this.valorInventario = valorInventario; }
    public long getUnidades() { return unidades; }
    public void setUnidades(long unidades) { this.unidades = unidades; }
    public long getProductosActivos() { return productosActivos; }
    public void setProductosActivos(long productosActivos) { this.productosActivos = productosActivos; }
    public List<ProductoAlerta> getStockBajo() { return stockBajo; }
    public void setStockBajo(List<ProductoAlerta> stockBajo) { this.stockBajo = stockBajo; }
    public List<LoteDTO> getLotesPorVencer() { return lotesPorVencer; }
    public void setLotesPorVencer(List<LoteDTO> lotesPorVencer) { this.lotesPorVencer = lotesPorVencer; }
    public List<LoteDTO> getLotesVencidos() { return lotesVencidos; }
    public void setLotesVencidos(List<LoteDTO> lotesVencidos) { this.lotesVencidos = lotesVencidos; }
    public int getDiasAlerta() { return diasAlerta; }
}
