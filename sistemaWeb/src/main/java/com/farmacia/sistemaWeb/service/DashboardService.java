package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.DashboardStatsDTO;
import com.farmacia.sistemaWeb.repository.CitaRepository;
import com.farmacia.sistemaWeb.repository.ConsultaRepository;
import com.farmacia.sistemaWeb.repository.PacienteRepository;
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
