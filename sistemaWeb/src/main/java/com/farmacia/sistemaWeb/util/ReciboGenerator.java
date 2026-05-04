package com.farmacia.sistemaWeb.util;

import com.farmacia.sistemaWeb.entity.DetalleVenta;
import com.farmacia.sistemaWeb.entity.Venta;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;

public class ReciboGenerator {

    public static byte[] generarRecibo(Venta venta) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font seccionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
            Font cuerpoFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);

            Paragraph titulo = new Paragraph("Petyzoos - Recibo de Venta", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Código de Venta: " + venta.getCodigoVenta(), seccionFont));
            document.add(new Paragraph("Fecha: " + venta.getFecha(), cuerpoFont));

            if (venta.getCliente() != null) {
                document.add(new Paragraph(
                        "Cliente: " + venta.getCliente().getNombres() + " " + venta.getCliente().getApellidos(),
                        cuerpoFont));
                document.add(new Paragraph("DNI: " + venta.getCliente().getDni(), cuerpoFont));
            }

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("DETALLE:", seccionFont));
            document.add(new Paragraph("----------------------------------------------"));

            for (DetalleVenta d : venta.getDetalles()) {
                document.add(new Paragraph(
                        String.format("%s: %s x S/%.2f = S/%.2f",
                                d.getCodigoDetalle(),
                                d.getProducto().getNombre(),
                                d.getProducto().getPrecioVenta(),
                                d.getPrecio()),
                        cuerpoFont));
            }

            document.add(new Paragraph("----------------------------------------------"));
            document.add(new Paragraph("TOTAL: S/" + String.format("%.2f", venta.getTotal()), seccionFont));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar recibo PDF: " + e.getMessage());
        }
    }
}
