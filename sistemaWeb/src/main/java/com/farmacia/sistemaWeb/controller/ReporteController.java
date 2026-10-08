package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.entity.Cliente;
import com.farmacia.sistemaWeb.entity.Paciente;
import com.farmacia.sistemaWeb.repository.ClienteRepository;
import com.farmacia.sistemaWeb.repository.PacienteRepository;
import com.farmacia.sistemaWeb.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * ReporteController — Endpoints de exportación a Excel (Apache POI).
 *
 * <p>Consumido por ReporteService del frontend Angular:
 * <ul>
 *   <li>GET /api/reportes/exportar-clientes  → clientes.xlsx</li>
 *   <li>GET /api/reportes/exportar-pacientes → pacientes.xlsx</li>
 * </ul>
 * Requiere rol ADMIN (regla en SecurityConfig).
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final MediaType EXCEL_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    @Autowired
    private ExcelService excelService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @GetMapping("/exportar-clientes")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        ByteArrayInputStream excel = excelService.generarReporteClientes(clientes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clientes.xlsx")
                .contentType(EXCEL_TYPE)
                .body(excel.readAllBytes());
    }

    @GetMapping("/exportar-pacientes")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarPacientes() {
        List<Paciente> pacientes = pacienteRepository.findAll();
        ByteArrayInputStream excel = excelService.generarReportePacientes(pacientes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pacientes.xlsx")
                .contentType(EXCEL_TYPE)
                .body(excel.readAllBytes());
    }
}
