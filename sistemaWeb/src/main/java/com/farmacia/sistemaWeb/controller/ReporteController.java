package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.VentaResponseDTO;
import com.farmacia.sistemaWeb.service.ReporteService;
import com.farmacia.sistemaWeb.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.farmacia.sistemaWeb.service.ExcelService;
import com.farmacia.sistemaWeb.service.ClienteService;
import com.farmacia.sistemaWeb.service.PacienteService;
import com.farmacia.sistemaWeb.repository.CitaRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

        @Autowired
        private ReporteService reporteService;
        @Autowired
        private CitaRepository citaRepository;
        @Autowired
        private ExcelService excelService;
        @Autowired
        private VentaService ventaService;
        @Autowired
        private ClienteService clienteService;
        @Autowired
        private PacienteService pacienteService;

        @GetMapping("/total-ventas")
        public ResponseEntity<Double> totalVentas() {
                return ResponseEntity.ok(reporteService.obtenerTotalDeVentas());
        }

        @GetMapping("/ventas-por-fecha")
        public ResponseEntity<List<VentaResponseDTO>> ventasPorFecha(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
                List<VentaResponseDTO> dtos = reporteService.obtenerVentasPorFecha(inicio, fin)
                                .stream().map(ventaService::mapToResponseDTO).collect(Collectors.toList());
                return ResponseEntity.ok(dtos);
        }

        @GetMapping("/ventas-por-cliente/{clienteDni}")
        public ResponseEntity<List<VentaResponseDTO>> ventasPorCliente(@PathVariable String clienteDni) {
                List<VentaResponseDTO> dtos = reporteService.obtenerVentasPorCliente(clienteDni)
                                .stream().map(ventaService::mapToResponseDTO).collect(Collectors.toList());
                return ResponseEntity.ok(dtos);
        }

        @GetMapping("/productos-mas-vendidos")
        public ResponseEntity<List<Map<String, Object>>> masVendidos() {
                return ResponseEntity.ok(reporteService.productosMasVendidos());
        }

        @GetMapping("/especies-mas-atendidas")
        public ResponseEntity<List<Map<String, Object>>> especiesMasAtendidas() {
                return ResponseEntity.ok(reporteService.especiesMasAtendidas());
        }

        @GetMapping("/cita/{codigoCita}/pdf")
        public ResponseEntity<byte[]> generarComprobanteCita(@PathVariable String codigoCita) {
                try {
                        com.farmacia.sistemaWeb.entity.Cita cita = citaRepository.findById(codigoCita)
                                        .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

                        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                        com.itextpdf.text.pdf.PdfWriter.getInstance(document, out);
                        document.open();

                        com.itextpdf.text.Font tituloFont = com.itextpdf.text.FontFactory.getFont(
                                        com.itextpdf.text.FontFactory.HELVETICA_BOLD, 18,
                                        com.itextpdf.text.BaseColor.BLACK);
                        com.itextpdf.text.Font subTituloFont = com.itextpdf.text.FontFactory.getFont(
                                        com.itextpdf.text.FontFactory.HELVETICA_BOLD, 14,
                                        com.itextpdf.text.BaseColor.DARK_GRAY);
                        com.itextpdf.text.Font cuerpoFont = com.itextpdf.text.FontFactory.getFont(
                                        com.itextpdf.text.FontFactory.HELVETICA, 12, com.itextpdf.text.BaseColor.BLACK);
                        com.itextpdf.text.Font destacadoFont = com.itextpdf.text.FontFactory.getFont(
                                        com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16,
                                        com.itextpdf.text.BaseColor.BLUE);

                        com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("Petyzoos", tituloFont);
                        titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        document.add(titulo);

                        com.itextpdf.text.Paragraph ruc = new com.itextpdf.text.Paragraph("RUC: 20123456789",
                                        subTituloFont);
                        ruc.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        document.add(ruc);

                        com.itextpdf.text.Paragraph direccion = new com.itextpdf.text.Paragraph(
                                        "Av. Principal 123, Lima - Perú", cuerpoFont);
                        direccion.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        document.add(direccion);

                        document.add(new com.itextpdf.text.Paragraph("\n"));

                        document.add(new com.itextpdf.text.Paragraph(
                                        "Código de Cita: " + cita.getCodigoCita(), subTituloFont));

                        document.add(new com.itextpdf.text.Paragraph(
                                        "Hola, " + cita.getPaciente().getCliente().getNombres() + ",", cuerpoFont));
                        document.add(new com.itextpdf.text.Paragraph(
                                        "Gracias por confiar en nosotros para el cuidado de tu mascota.", cuerpoFont));

                        document.add(new com.itextpdf.text.Paragraph(
                                        "\n------------------------------------------------\n"));

                        com.itextpdf.text.Paragraph fechaLabel = new com.itextpdf.text.Paragraph(
                                        "SU CITA ESTÁ PROGRAMADA PARA:", subTituloFont);
                        fechaLabel.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        document.add(fechaLabel);

                        com.itextpdf.text.Paragraph fechaValor = new com.itextpdf.text.Paragraph(
                                        cita.getFecha() + " a las " + cita.getHora(), destacadoFont);
                        fechaValor.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        document.add(fechaValor);

                        document.add(new com.itextpdf.text.Paragraph(
                                        "\n------------------------------------------------\n"));

                        document.add(new com.itextpdf.text.Paragraph("DETALLES DEL SERVICIO:", subTituloFont));
                        document.add(new com.itextpdf.text.Paragraph("\n"));

                        document.add(new com.itextpdf.text.Paragraph(
                                        "Cliente: " + cita.getPaciente().getCliente().getNombres() + " "
                                                        + cita.getPaciente().getCliente().getApellidos(),
                                        cuerpoFont));
                        document.add(new com.itextpdf.text.Paragraph(
                                        "Paciente: " + cita.getPaciente().getNombre() + " ("
                                                        + cita.getPaciente().getEspecie() + " - "
                                                        + cita.getPaciente().getRaza() + ")",
                                        cuerpoFont));
                        document.add(new com.itextpdf.text.Paragraph(
                                        "Veterinario: Dr. " + cita.getVeterinario().getNombres(), cuerpoFont));
                        document.add(new com.itextpdf.text.Paragraph("Motivo: " + cita.getMotivo(), cuerpoFont));
                        document.add(new com.itextpdf.text.Paragraph("Tiempo estimado de servicio: "
                                        + (cita.getDuracionMinutos() != null ? cita.getDuracionMinutos() : 30)
                                        + " minutos", cuerpoFont));

                        document.add(new com.itextpdf.text.Paragraph("\n\n"));

                        com.itextpdf.text.Paragraph footer = new com.itextpdf.text.Paragraph(
                                        "Por favor, llegar 10 minutos antes de su cita.", cuerpoFont);
                        footer.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        document.add(footer);

                        document.close();

                        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
                        headers.setContentDispositionFormData("attachment", "comprobante_cita_" + codigoCita + ".pdf");

                        return new ResponseEntity<>(out.toByteArray(), headers, org.springframework.http.HttpStatus.OK);

                } catch (Exception e) {
                        e.printStackTrace();
                        return new ResponseEntity<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        @GetMapping("/exportar-clientes")
        public ResponseEntity<InputStreamResource> exportarClientes() {
                java.io.ByteArrayInputStream in = excelService.generarReporteClientes(clienteService.listarClientes());

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Disposition", "attachment; filename=clientes.xlsx");

                return ResponseEntity.ok().headers(headers)
                                .contentType(MediaType.parseMediaType(
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                                .body(new InputStreamResource(in));
        }

        @GetMapping("/exportar-pacientes")
        public ResponseEntity<InputStreamResource> exportarPacientes() {
                java.io.ByteArrayInputStream in = excelService.generarReportePacientes(pacienteService.listarEntidades());

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Disposition", "attachment; filename=pacientes.xlsx");

                return ResponseEntity.ok().headers(headers)
                                .contentType(MediaType.parseMediaType(
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                                .body(new InputStreamResource(in));
        }
}
