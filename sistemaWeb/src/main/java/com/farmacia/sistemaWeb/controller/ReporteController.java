package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.entity.Venta;
import com.farmacia.sistemaWeb.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping("/total-ventas")
    public ResponseEntity<Double> totalVentas() {
        return ResponseEntity.ok(reporteService.obtenerTotalDeVentas());
    }

    @GetMapping("/ventas-por-fecha")
    public ResponseEntity<List<Venta>> ventasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        return ResponseEntity.ok(reporteService.obtenerVentasPorFecha(inicio, fin));
    }

    @GetMapping("/ventas-por-cliente/{clienteId}")
    public ResponseEntity<List<Venta>> ventasPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(reporteService.obtenerVentasPorCliente(clienteId));
    }

    @GetMapping("/medicamentos-mas-vendidos")
    public ResponseEntity<List<Map<String, Object>>> masVendidos() {
        return ResponseEntity.ok(reporteService.medicamentosMasVendidos());
    }



}
