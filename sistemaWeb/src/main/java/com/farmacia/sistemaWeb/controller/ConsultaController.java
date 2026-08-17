package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.ConsultaDTO;
import com.farmacia.sistemaWeb.dto.ConsultaResponseDTO;
import com.farmacia.sistemaWeb.entity.Consulta;
import com.farmacia.sistemaWeb.service.ConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaService consultaService;



    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody ConsultaDTO dto) {
        try {
            ConsultaResponseDTO nueva = consultaService.registrarConsulta(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/por-dni/{dni}")
    public ResponseEntity<List<ConsultaResponseDTO>> buscarPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(consultaService.buscarConsultasPorDniCliente(dni));
    }

    @GetMapping
    public ResponseEntity<List<ConsultaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(consultaService.listarConsultas());
    }

    @GetMapping("/historial/paciente/{codigoPaciente}")
    public ResponseEntity<List<ConsultaResponseDTO>> obtenerHistorialPorPaciente(@PathVariable String codigoPaciente) {
        return ResponseEntity.ok(consultaService.obtenerHistorialPorPaciente(codigoPaciente));
    }

    @GetMapping("/hoy")
    public ResponseEntity<List<ConsultaResponseDTO>> listarConsultasHoy() {
        return ResponseEntity.ok(consultaService.listarConsultasHoy());
    }

    @GetMapping("/{codigoConsulta}/receta/pdf")
    public ResponseEntity<byte[]> generarRecetaMedica(@PathVariable String codigoConsulta) {
        try {
            Consulta consulta = consultaService.buscarPorCodigo(codigoConsulta);

            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, out);
            document.open();

            com.itextpdf.text.Font tituloFont = com.itextpdf.text.FontFactory
                    .getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 18, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font subTituloFont = com.itextpdf.text.FontFactory
                    .getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 14, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font cuerpoFont = com.itextpdf.text.FontFactory
                    .getFont(com.itextpdf.text.FontFactory.HELVETICA, 12, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font destacadoFont = com.itextpdf.text.FontFactory
                    .getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 12, com.itextpdf.text.BaseColor.BLUE);

            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("Petyzoos - Receta Médica",
                    tituloFont);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(titulo);

            com.itextpdf.text.Paragraph ruc = new com.itextpdf.text.Paragraph(
                    "RUC: 20123456789\nAv. Principal 123, Lima - Perú", cuerpoFont);
            ruc.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(ruc);

            document.add(new com.itextpdf.text.Paragraph("\n------------------------------------------------\n"));

            document.add(new com.itextpdf.text.Paragraph("Código de Consulta: " + consulta.getCodigoConsulta(),
                    subTituloFont));
            document.add(new com.itextpdf.text.Paragraph("Fecha: " + consulta.getFecha().toString(), cuerpoFont));

            String especieNombre = consulta.getPaciente().getRaza() != null && consulta.getPaciente().getRaza().getEspecie() != null
                    ? consulta.getPaciente().getRaza().getEspecie().getNombre() : "N/A";

            document.add(new com.itextpdf.text.Paragraph("\nPaciente: " + consulta.getPaciente().getNombre() + " ("
                    + especieNombre + ")", destacadoFont));
            document.add(new com.itextpdf.text.Paragraph("Cliente: " + consulta.getPaciente().getCliente().getNombres()
                    + " " + consulta.getPaciente().getCliente().getApellidos(), cuerpoFont));
            document.add(new com.itextpdf.text.Paragraph("Veterinario: " + consulta.getVeterinario().getNombres() + " "
                    + consulta.getVeterinario().getApellidos(), cuerpoFont));

            document.add(new com.itextpdf.text.Paragraph("\n--- DATOS CLÍNICOS ---", subTituloFont));
            document.add(new com.itextpdf.text.Paragraph("Motivo: " + consulta.getMotivo(), cuerpoFont));
            document.add(new com.itextpdf.text.Paragraph(
                    "Diagnóstico: " + (consulta.getDiagnostico() != null ? consulta.getDiagnostico() : "No registrado"),
                    cuerpoFont));

            document.add(new com.itextpdf.text.Paragraph("\n--- TRATAMIENTO Y RECETA ---", subTituloFont));
            document.add(new com.itextpdf.text.Paragraph(
                    consulta.getTratamiento() != null ? consulta.getTratamiento() : "Sin tratamiento especificado",
                    cuerpoFont));

            document.add(new com.itextpdf.text.Paragraph("\n\n\n\n_______________________\nFirma del Veterinario",
                    cuerpoFont));
            document.close();

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "receta_" + codigoConsulta + ".pdf");

            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}