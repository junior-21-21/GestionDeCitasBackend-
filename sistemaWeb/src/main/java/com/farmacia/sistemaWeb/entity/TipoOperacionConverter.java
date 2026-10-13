package com.farmacia.sistemaWeb.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Guarda el tipo de operación del kardex como texto plano (mismo valor que antes con @Enumerated(STRING)). */
@Converter
public class TipoOperacionConverter implements AttributeConverter<Kardex.TipoOperacion, String> {

    @Override
    public String convertToDatabaseColumn(Kardex.TipoOperacion tipo) {
        return tipo == null ? null : tipo.name();
    }

    @Override
    public Kardex.TipoOperacion convertToEntityAttribute(String valor) {
        return valor == null ? null : Kardex.TipoOperacion.valueOf(valor.trim());
    }
}
