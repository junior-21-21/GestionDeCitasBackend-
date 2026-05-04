package com.farmacia.sistemaWeb.service;

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

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;
    @Autowired
    private EntityManager entityManager;

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

    public List<Venta> obtenerVentasPorCliente(String clienteDni) {
        return ventaRepository.findAll()
                .stream()
                .filter(v -> v.getCliente().getDni().equals(clienteDni))
                .toList();
    }

    public List<Map<String, Object>> productosMasVendidos() {
        String jpql = """
                    SELECT d.producto.nombre AS nombre, SUM(d.cantidad) AS total
                    FROM DetalleVenta d
                    GROUP BY d.producto.nombre
                    ORDER BY total DESC
                """;

        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        List<Object[]> resultados = query.getResultList();

        List<Map<String, Object>> reporte = new ArrayList<>();
        for (Object[] fila : resultados) {
            Map<String, Object> map = new HashMap<>();
            map.put("producto", fila[0]);
            map.put("cantidad", fila[1]);
            reporte.add(map);
        }

        return reporte;
    }

    public List<Map<String, Object>> especiesMasAtendidas() {
        String jpql = """
                    SELECT c.paciente.especie AS especie, COUNT(c) AS total
                    FROM Consulta c
                    GROUP BY c.paciente.especie
                    ORDER BY total DESC
                """;

        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        List<Object[]> resultados = query.getResultList();

        List<Map<String, Object>> reporte = new ArrayList<>();
        for (Object[] fila : resultados) {
            Map<String, Object> map = new HashMap<>();
            map.put("especie", fila[0]);
            map.put("cantidad", fila[1]);
            reporte.add(map);
        }

        return reporte;
    }
}
