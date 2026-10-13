package com.farmacia.sistemaWeb.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Las columnas nuevas de tipo enumerado se guardan como texto con un converter (y no con @Enumerated):
 * así Hibernate no crea un CHECK con la lista de valores en SQL Server, y agregar un valor nuevo
 * al enum nunca rompe los INSERT.
 */
public final class EnumTextoConverters {

    private EnumTextoConverters() {}

    private static <E extends Enum<E>> E leer(Class<E> tipo, String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Enum.valueOf(tipo, valor.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Converter
    public static class Clase implements AttributeConverter<ClaseProducto, String> {
        @Override public String convertToDatabaseColumn(ClaseProducto v) { return v == null ? null : v.name(); }
        @Override public ClaseProducto convertToEntityAttribute(String v) { return leer(ClaseProducto.class, v); }
    }

    @Converter
    public static class Linea implements AttributeConverter<ClaseProducto.LineaNegocio, String> {
        @Override public String convertToDatabaseColumn(ClaseProducto.LineaNegocio v) { return v == null ? null : v.name(); }
        @Override public ClaseProducto.LineaNegocio convertToEntityAttribute(String v) { return leer(ClaseProducto.LineaNegocio.class, v); }
    }

    @Converter
    public static class Unidad implements AttributeConverter<UnidadBase, String> {
        @Override public String convertToDatabaseColumn(UnidadBase v) { return v == null ? null : v.name(); }
        @Override public UnidadBase convertToEntityAttribute(String v) { return leer(UnidadBase.class, v); }
    }

    @Converter
    public static class NombreRol implements AttributeConverter<Rol.NombreRol, String> {
        @Override public String convertToDatabaseColumn(Rol.NombreRol v) { return v == null ? null : v.name(); }
        @Override public Rol.NombreRol convertToEntityAttribute(String v) { return v == null ? null : Rol.NombreRol.valueOf(v.trim()); }
    }
}
