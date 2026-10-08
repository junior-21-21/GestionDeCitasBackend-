package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.DashboardStatsDTO;
import com.farmacia.sistemaWeb.repository.CitaRepository;
import com.farmacia.sistemaWeb.repository.ConsultaRepository;
import com.farmacia.sistemaWeb.repository.PacienteRepository;
import com.farmacia.sistemaWeb.repository.VentaRepository;
import com.farmacia.sistemaWeb.repository.CobroConsultaRepository;
import com.farmacia.sistemaWeb.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private CobroConsultaRepository cobroRepository;

    @Autowired
    private CompraRepository compraRepository;

    public DashboardStatsDTO getStats(String periodo) {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        LocalDate now = LocalDate.now();
        LocalDate start;
        LocalDate end = now;

        if ("hoy".equalsIgnoreCase(periodo)) {
            stats.setCitas(citaRepository.countByFecha(now));
            stats.setConsultas(consultaRepository.countByFecha(now));
            stats.setPacientes(pacienteRepository.count());
        } else if ("semana".equalsIgnoreCase(periodo)) {
            start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            end = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            stats.setCitas(citaRepository.countByFechaBetween(start, end));
            stats.setConsultas(consultaRepository.countByFechaBetween(start, end));
            stats.setPacientes(pacienteRepository.count());
        } else if ("mes".equalsIgnoreCase(periodo)) {
            start = now.with(TemporalAdjusters.firstDayOfMonth());
            end = now.with(TemporalAdjusters.lastDayOfMonth());
            stats.setCitas(citaRepository.countByFechaBetween(start, end));
            stats.setConsultas(consultaRepository.countByFechaBetween(start, end));
            stats.setPacientes(pacienteRepository.count());
        } else { // total
            stats.setCitas(citaRepository.count());
            stats.setConsultas(consultaRepository.count());
            stats.setPacientes(pacienteRepository.count());
        }

        // Calcular ventas y compras
        Double ventasPOS = 0.0;
        Double ventasConsultas = 0.0;
        java.math.BigDecimal compras = java.math.BigDecimal.ZERO;

        if ("hoy".equalsIgnoreCase(periodo) || "semana".equalsIgnoreCase(periodo) || "mes".equalsIgnoreCase(periodo)) {
            java.time.LocalDateTime inicio = start.atStartOfDay();
            java.time.LocalDateTime fin = end.atTime(23, 59, 59);

            ventasPOS = ventaRepository.sumTotalByFechaBetween(inicio, fin);
            ventasConsultas = cobroRepository.sumTotalByFechaBetween(inicio, fin);
            compras = compraRepository.sumTotalByFechaRegistroBetween(inicio, fin);
        } else {
            ventasPOS = ventaRepository.sumTotal();
            ventasConsultas = cobroRepository.sumTotal();
            compras = compraRepository.sumTotal();
        }

        stats.setVentas((ventasPOS != null ? ventasPOS : 0.0) + (ventasConsultas != null ? ventasConsultas : 0.0));
        stats.setCompras(compras != null ? compras.doubleValue() : 0.0);

        List<Object[]> topEspeciesRaw = pacienteRepository.findTopEspecies();
        Map<String, Long> topEspecies = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(topEspeciesRaw.size(), 3); i++) {
            Object[] row = topEspeciesRaw.get(i);
            String nombre = (String) row[0];
            Long count = (Long) row[1];
            topEspecies.put(nombre, count);
        }
        stats.setTopEspecies(topEspecies);

        return stats;
    }
}
