package com.farmacia.sistemaWeb.entity;

/**
 * Clase del producto: define la línea de negocio, las reglas por defecto (receta, controlado, cadena de frío,
 * lote y vencimiento obligatorios) y el {@link Producto.TipoProducto} antiguo, que se sigue guardando por compatibilidad.
 */
public enum ClaseProducto {
    MEDICAMENTO_LIBRE("Medicamento de libre venta", LineaNegocio.FARMACIA, false, false, false, true, Producto.TipoProducto.VENTA_PUBLICO),
    MEDICAMENTO_RECETA("Medicamento con receta", LineaNegocio.FARMACIA, true, false, false, true, Producto.TipoProducto.VENTA_PUBLICO),
    MEDICAMENTO_CONTROLADO("Medicamento controlado", LineaNegocio.FARMACIA, true, true, false, true, Producto.TipoProducto.VENTA_PUBLICO),
    VACUNA("Vacuna y biológico", LineaNegocio.FARMACIA, false, false, true, true, Producto.TipoProducto.INSUMO_MEDICO),
    INSUMO_CLINICO("Insumo clínico", LineaNegocio.FARMACIA, false, false, false, false, Producto.TipoProducto.INSUMO_MEDICO),
    ALIMENTO("Alimento y snack", LineaNegocio.TIENDA, false, false, false, true, Producto.TipoProducto.VENTA_PUBLICO),
    ACCESORIO("Accesorio y juguete", LineaNegocio.TIENDA, false, false, false, false, Producto.TipoProducto.VENTA_PUBLICO),
    HIGIENE("Higiene y cuidado", LineaNegocio.TIENDA, false, false, false, false, Producto.TipoProducto.VENTA_PUBLICO),
    SERVICIO("Servicio", LineaNegocio.SERVICIOS, false, false, false, false, Producto.TipoProducto.SERVICIO);

    private final String etiqueta;
    private final LineaNegocio linea;
    private final boolean receta;
    private final boolean controlado;
    private final boolean refrigerado;
    private final boolean lote;
    private final Producto.TipoProducto tipo;

    ClaseProducto(String etiqueta, LineaNegocio linea, boolean receta, boolean controlado, boolean refrigerado,
                  boolean lote, Producto.TipoProducto tipo) {
        this.etiqueta = etiqueta;
        this.linea = linea;
        this.receta = receta;
        this.controlado = controlado;
        this.refrigerado = refrigerado;
        this.lote = lote;
        this.tipo = tipo;
    }

    public String getEtiqueta() { return etiqueta; }
    public LineaNegocio getLinea() { return linea; }
    public boolean isRecetaPorDefecto() { return receta; }
    public boolean isControladoPorDefecto() { return controlado; }
    public boolean isRefrigeradoPorDefecto() { return refrigerado; }
    /** Lote y vencimiento obligatorios en cada ingreso (farmacia, salvo insumos, y alimentos). */
    public boolean isLotePorDefecto() { return lote; }
    public Producto.TipoProducto getTipo() { return tipo; }

    public enum LineaNegocio {
        FARMACIA, TIENDA, SERVICIOS
    }
}
