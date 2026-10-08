package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.entity.Venta;
import com.farmacia.sistemaWeb.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public List<Venta> listarVentas() {
        return ventaService.listarVentas();
    }

    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody Venta ventaRequest) {
        try {
            Venta nuevaVenta = ventaService.registrarVentaDirecta(ventaRequest);
            return ResponseEntity.ok(nuevaVenta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ocurrió un error al procesar la venta");
        }
    }

    @GetMapping("/{id}/comprobante/pdf")
    public ResponseEntity<byte[]> generarComprobante(@PathVariable Long id) {
        try {
            Venta venta = ventaService.obtenerVentaPorId(id);
            
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

            try {
                com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance("C:\\Users\\quica\\Desktop\\Proyecto gestion de citas\\GestionDeCitasFront\\public\\logo_sysvet.png");
                logo.scaleToFit(150, 150);
                logo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                System.out.println("No se pudo cargar el logo: " + e.getMessage());
            }

            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("Boleta de Venta Electrónica", tituloFont);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(titulo);

            com.itextpdf.text.Paragraph info = new com.itextpdf.text.Paragraph(
                    "\nNro Venta: " + venta.getId() +
                    "\nFecha: " + venta.getFecha().toString() +
                    "\nCliente: " + venta.getClienteNombre() + "\n\n", cuerpoFont);
            document.add(info);

            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(4);
            table.setWidthPercentage(100);
            
            String[] headersArr = {"Producto", "Precio Unit.", "Cantidad", "Subtotal"};
            for (String header : headersArr) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, tableHeaderFont));
                cell.setBackgroundColor(com.itextpdf.text.BaseColor.DARK_GRAY);
                table.addCell(cell);
            }

            if (venta.getDetalles() != null) {
                for (com.farmacia.sistemaWeb.entity.DetalleVenta item : venta.getDetalles()) {
                    table.addCell(item.getProducto().getNombre());
                    table.addCell("S/ " + String.format("%.2f", item.getPrecioUnitario()));
                    table.addCell(String.valueOf(item.getCantidad()));
                    table.addCell("S/ " + String.format("%.2f", item.getSubtotal()));
                }
            }
            document.add(table);

            com.itextpdf.text.Paragraph total = new com.itextpdf.text.Paragraph(
                    "\nTOTAL PAGADO: S/ " + String.format("%.2f", venta.getTotal()), 
                    com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 14, com.itextpdf.text.BaseColor.RED));
            total.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(total);

            document.close();

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "boleta_venta_" + id + ".pdf");

            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
