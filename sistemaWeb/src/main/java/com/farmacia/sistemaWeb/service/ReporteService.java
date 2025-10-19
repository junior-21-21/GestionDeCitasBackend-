package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.DetalleVenta;
import com.farmacia.sistemaWeb.entity.Venta;
import com.farmacia.sistemaWeb.repository.DetalleVentaRepository;
import com.farmacia.sistemaWeb.repository.VentaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ReporteService {

    @Autowired private VentaRepository ventaRepository;
    @Autowired private DetalleVentaRepository detalleVentaRepository;
    @Autowired private EntityManager entityManager;

    public double obtenerTotalDeVentas() {
        return ventaRepository.findAll()
                .stream()
                .mapToDouble(Venta::getTotal)
                .sum();
    }

    public List<Venta> obtenerVentasPorFecha(LocalDate inicio, LocalDate fin) {
        return ventaRepository.findAll()
                .stream()
                .filter(v -> !v.getFecha().isBefore(inicio) && !v.getFecha().isAfter(fin))
                .toList();
    }

    public List<Venta> obtenerVentasPorCliente(Long clienteId) {
        return ventaRepository.findAll()
                .stream()
                .filter(v -> v.getCliente().getId().equals(clienteId))
                .toList();
    }

    public List<Map<String, Object>> medicamentosMasVendidos() {
        String jpql = """
            SELECT d.medicamento.nombre AS nombre, SUM(d.cantidad) AS total
            FROM DetalleVenta d
            GROUP BY d.medicamento.nombre
            ORDER BY total DESC
        """;

        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        List<Object[]> resultados = query.getResultList();

        List<Map<String, Object>> reporte = new ArrayList<>();
        for (Object[] fila : resultados) {
            Map<String, Object> map = new HashMap<>();
            map.put("medicamento", fila[0]);
            map.put("cantidad", fila[1]);
            reporte.add(map);
        }

        return reporte;
    }
}
