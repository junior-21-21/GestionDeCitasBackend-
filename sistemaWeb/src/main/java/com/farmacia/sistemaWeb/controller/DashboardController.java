package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.DashboardStatsDTO;
import com.farmacia.sistemaWeb.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats(
            @RequestParam(defaultValue = "hoy") String periodo) {
        DashboardStatsDTO stats = dashboardService.getStats(periodo);
        // El groomer ve la actividad de la clínica, no los montos de caja
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_GROOMER".equals(a.getAuthority()))) {
            stats.setVentas(null);
            stats.setCompras(null);
        }
        return ResponseEntity.ok(stats);
    }
}
