package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.VentaDTO;
import com.farmacia.sistemaWeb.dto.VentaResponseDTO;
import com.farmacia.sistemaWeb.entity.Venta;
import com.farmacia.sistemaWeb.util.ReciboGenerator;
import com.farmacia.sistemaWeb.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @PostMapping
    public ResponseEntity<VentaResponseDTO> registrarVenta(@RequestBody VentaDTO dto) {
        Venta venta = ventaService.registrarVenta(dto);
        return ResponseEntity.ok(ventaService.mapToResponseDTO(venta));
    }

    @GetMapping
    public List<VentaResponseDTO> listarVentas() {
        return ventaService.listarVentas().stream()
                .map(ventaService::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/recibo/{codigoVenta}")
    public ResponseEntity<byte[]> generarReciboPDF(@PathVariable String codigoVenta) {
        Venta venta = ventaService.obtenerPorCodigo(codigoVenta);

        byte[] pdfBytes = ReciboGenerator.generarRecibo(venta);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recibo_venta_" + codigoVenta + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
