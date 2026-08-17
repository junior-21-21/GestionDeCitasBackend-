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
        return ResponseEntity.ok(dashboardService.getStats(periodo));
    }
}
