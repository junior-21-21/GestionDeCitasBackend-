package com.farmacia.sistemaWeb.entity;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

public class ReciboGenerator {

    public static byte[] generarRecibo(Venta venta) {
        try {
            Document documento = new Document();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(documento, baos);
            documento.open();

            Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

            documento.add(new Paragraph("FARMACIA ROMA", negrita));
            documento.add(new Paragraph("RUC: 123456789", normal));
            documento.add(new Paragraph("Dirección: Calle Principal N° 123", normal));
            documento.add(new Paragraph("Teléfono: 987654321", normal));
            documento.add(Chunk.NEWLINE);

            documento.add(new Paragraph("RECIBO DE VENTA", negrita));
            documento.add(new Paragraph("Fecha: " + venta.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), normal));
            documento.add(new Paragraph("Cliente: " + venta.getCliente().getNombres() + " " + venta.getCliente().getApellidos(), normal));
            documento.add(new Paragraph("DNI: " + venta.getCliente().getDni(), normal));
            documento.add(Chunk.NEWLINE);

            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{3, 1, 1, 1});
            tabla.addCell(new PdfPCell(new Phrase("Medicamento", negrita)));
            tabla.addCell(new PdfPCell(new Phrase("Cantidad", negrita)));
            tabla.addCell(new PdfPCell(new Phrase("P. Unitario", negrita)));
            tabla.addCell(new PdfPCell(new Phrase("Subtotal", negrita)));

            for (DetalleVenta d : venta.getDetalles()) {
                tabla.addCell(new Phrase(d.getMedicamento().getNombre(), normal));
                tabla.addCell(new Phrase(String.valueOf(d.getCantidad()), normal));
                tabla.addCell(new Phrase(String.format("S/ %.2f", d.getPrecio()), normal));
                double subtotal = d.getPrecio() * d.getCantidad();
                tabla.addCell(new Phrase(String.format("S/ %.2f", subtotal), normal));
            }

            documento.add(tabla);
            documento.add(Chunk.NEWLINE);
            documento.add(new Paragraph("TOTAL: S/ " + String.format("%.2f", venta.getTotal()), negrita));

            documento.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar recibo: " + e.getMessage(), e);
        }
    }
}