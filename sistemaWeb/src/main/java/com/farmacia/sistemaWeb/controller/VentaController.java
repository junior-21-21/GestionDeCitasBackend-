package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.VentaDTO;
import com.farmacia.sistemaWeb.entity.ReciboGenerator;
import com.farmacia.sistemaWeb.entity.Venta;
import com.farmacia.sistemaWeb.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @PostMapping
    public ResponseEntity<Venta> registrarVenta(@RequestBody VentaDTO dto) {
        return ResponseEntity.ok(ventaService.registrarVenta(dto));
    }

    @GetMapping
    public List<Venta> listarVentas() {
        return ventaService.listarVentas();
    }

    @GetMapping("/recibo/{id}")
    public ResponseEntity<byte[]> generarReciboPDF(@PathVariable Long id) {
        Venta venta = ventaService.obtenerPorId(id); // Asegúrate que este método existe

        byte[] pdfBytes = ReciboGenerator.generarRecibo(venta);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recibo_venta_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}
