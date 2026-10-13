package com.farmacia.sistemaWeb.entity;

/**
 * Unidad en la que se cuenta el stock de un producto. Stock, lotes y kardex son enteros en esta unidad;
 * las presentaciones (caja, blíster, kilo...) indican cuántas unidades base contienen.
 */
public enum UnidadBase {
    UNIDAD("Unidad", "unidades", false),
    TABLETA("Tableta", "tabletas", false),
    CAPSULA("Cápsula", "cápsulas", false),
    ML("Mililitro", "ml", true),
    GRAMO("Gramo", "g", true),
    DOSIS("Dosis", "dosis", false),
    PIPETA("Pipeta", "pipetas", false),
    SOBRE("Sobre", "sobres", false),
    AMPOLLA("Ampolla", "ampollas", false),
    FRASCO("Frasco", "frascos", false);

    private final String etiqueta;
    private final String plural;
    private final boolean fraccionable;

    UnidadBase(String etiqueta, String plural, boolean fraccionable) {
        this.etiqueta = etiqueta;
        this.plural = plural;
        this.fraccionable = fraccionable;
    }

    public String getEtiqueta() { return etiqueta; }
    public String getPlural() { return plural; }
    /** Si admite vender cantidades con decimales de sus presentaciones (p. ej. 2.5 kg = 2500 g). */
    public boolean isFraccionable() { return fraccionable; }
}
