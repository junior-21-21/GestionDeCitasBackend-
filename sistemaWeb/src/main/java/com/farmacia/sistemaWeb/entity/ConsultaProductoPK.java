package com.farmacia.sistemaWeb.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ConsultaProductoPK implements Serializable {

    private String codigoConsulta;
    private String codigoBarras;

    public ConsultaProductoPK() {
    }

    public ConsultaProductoPK(String codigoConsulta, String codigoBarras) {
        this.codigoConsulta = codigoConsulta;
        this.codigoBarras = codigoBarras;
    }

    public String getCodigoConsulta() {
        return codigoConsulta;
    }

    public void setCodigoConsulta(String codigoConsulta) {
        this.codigoConsulta = codigoConsulta;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConsultaProductoPK that = (ConsultaProductoPK) o;
        return Objects.equals(codigoConsulta, that.codigoConsulta) && Objects.equals(codigoBarras, that.codigoBarras);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoConsulta, codigoBarras);
    }
}
