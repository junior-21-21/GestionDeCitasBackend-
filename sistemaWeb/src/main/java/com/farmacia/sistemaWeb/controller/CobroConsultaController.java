package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.entity.CobroConsulta;
import com.farmacia.sistemaWeb.service.CobroConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cobros")
public class CobroConsultaController {

    @Autowired
    private CobroConsultaService cobroService;

    @PostMapping("/consulta/{codigoConsulta}")
    public ResponseEntity<?> registrarCobro(@PathVariable String codigoConsulta, @RequestBody CobroConsulta cobro) {
        try {
            CobroConsulta nuevoCobro = cobroService.registrarCobro(codigoConsulta, cobro);
            return ResponseEntity.ok(nuevoCobro);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/consulta/{codigoConsulta}")
    public ResponseEntity<?> obtenerCobro(@PathVariable String codigoConsulta) {
        return cobroService.obtenerCobroPorConsulta(codigoConsulta)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<?> listarPendientes() {
        return ResponseEntity.ok(cobroService.listarCobrosPendientes());
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<?> pagarCobro(@PathVariable Long id) {
        try {
            CobroConsulta pagado = cobroService.pagarCobro(id);
            return ResponseEntity.ok(pagado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/comprobante/pdf")
    public ResponseEntity<byte[]> generarComprobante(@PathVariable Long id) {
        try {
            CobroConsulta cobro = cobroService.obtenerCobroPorId(id);
            
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, out);
            document.open();

            com.itextpdf.text.Font tituloFont = com.itextpdf.text.FontFactory
                    .getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 18, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font cuerpoFont = com.itextpdf.text.FontFactory
                    .getFont(com.itextpdf.text.FontFactory.HELVETICA, 12, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font tableHeaderFont = com.itextpdf.text.FontFactory
                    .getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.WHITE);

            // Intentar cargar logo
            try {
                com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance("C:\\Users\\quica\\Desktop\\Proyecto gestion de citas\\GestionDeCitasFront\\public\\logo_sysvet.png");
                logo.scaleToFit(150, 150);
                logo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                System.out.println("No se pudo cargar el logo: " + e.getMessage());
            }

            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("Comprobante de Pago", tituloFont);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(titulo);

            com.itextpdf.text.Paragraph info = new com.itextpdf.text.Paragraph(
                    "\nFecha: " + cobro.getFecha().toString() +
                    "\nConsulta: " + cobro.getConsulta().getCodigoConsulta() +
                    "\nEstado: " + cobro.getEstado() + "\n\n", cuerpoFont);
            document.add(info);

            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(4);
            table.setWidthPercentage(100);
            
            String[] headersArr = {"Descripción", "Precio Unit.", "Cantidad", "Subtotal"};
            for (String header : headersArr) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, tableHeaderFont));
                cell.setBackgroundColor(com.itextpdf.text.BaseColor.DARK_GRAY);
                table.addCell(cell);
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode detalles = mapper.readTree(cobro.getDetalleCargos());
            
            if (detalles.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode item : detalles) {
                    table.addCell(item.has("descripcion") ? item.get("descripcion").asText() : "");
                    table.addCell("S/ " + (item.has("precio") ? item.get("precio").asText() : "0.00"));
                    table.addCell(item.has("cantidad") ? item.get("cantidad").asText() : "0");
                    table.addCell("S/ " + (item.has("subtotal") ? item.get("subtotal").asText() : "0.00"));
                }
            }
            document.add(table);

            com.itextpdf.text.Paragraph total = new com.itextpdf.text.Paragraph(
                    "\nTOTAL PAGADO: S/ " + String.format("%.2f", cobro.getTotal()), 
                    com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 14, com.itextpdf.text.BaseColor.RED));
            total.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(total);

            document.close();

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            // Inline for opening in new tab
            headers.setContentDispositionFormData("inline", "comprobante_" + id + ".pdf");

            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
