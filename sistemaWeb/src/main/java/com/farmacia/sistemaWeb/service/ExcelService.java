package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.Cliente;
import com.farmacia.sistemaWeb.entity.Paciente;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

    public ByteArrayInputStream generarReporteClientes(List<Cliente> clientes) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Clientes");

            String[] headers = { "DNI", "Nombres", "Apellidos", "Teléfono", "Dirección" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(getHeaderCellStyle(workbook));
            }

            int rowIdx = 1;
            for (Cliente cliente : clientes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(cliente.getDni());
                row.createCell(1).setCellValue(cliente.getNombres());
                row.createCell(2).setCellValue(cliente.getApellidos());
                row.createCell(3).setCellValue(cliente.getTelefono());
                row.createCell(4).setCellValue(cliente.getDireccionCompleta());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel de clientes: " + e.getMessage());
        }
    }

    public ByteArrayInputStream generarReportePacientes(List<Paciente> pacientes) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Pacientes");

            String[] headers = { "Código", "Nombre", "Especie", "Raza", "Dueño" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(getHeaderCellStyle(workbook));
            }

            int rowIdx = 1;
            for (Paciente paciente : pacientes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(paciente.getCodigoPaciente());
                row.createCell(1).setCellValue(paciente.getNombre());
                row.createCell(2).setCellValue(paciente.getRaza() != null && paciente.getRaza().getEspecie() != null
                        ? paciente.getRaza().getEspecie().getNombre() : "N/A");
                row.createCell(3).setCellValue(paciente.getRaza() != null
                        ? paciente.getRaza().getNombre() : "N/A");

                String ownerName = "Sin Dueño";
                if (paciente.getCliente() != null) {
                    ownerName = paciente.getCliente().getNombres() + " " + paciente.getCliente().getApellidos();
                }
                row.createCell(4).setCellValue(ownerName);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel de pacientes: " + e.getMessage());
        }
    }

    private CellStyle getHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
