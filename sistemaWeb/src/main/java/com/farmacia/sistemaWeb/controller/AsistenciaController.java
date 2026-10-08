package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.entity.AsistenciaDiaria;
import com.farmacia.sistemaWeb.service.AsistenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.ByteArrayOutputStream;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.util.List;
import com.farmacia.sistemaWeb.entity.HorarioTrabajador;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/asistencia")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @GetMapping("/estado")
    public ResponseEntity<?> obtenerEstadoTurno(Authentication authentication) {
        AsistenciaDiaria turno = asistenciaService.obtenerTurnoActual(authentication.getName());
        return ResponseEntity.ok(java.util.Map.of("estado", turno != null ? turno.getEstado().name() : "CERRADO"));
    }

    @PostMapping("/abrir")
    public ResponseEntity<?> abrirTurno(Authentication authentication) {
        try {
            AsistenciaDiaria turno = asistenciaService.abrirTurno(authentication.getName());
            return ResponseEntity.ok(turno);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cerrar")
    public ResponseEntity<?> cerrarTurno(Authentication authentication) {
        try {
            AsistenciaDiaria turno = asistenciaService.cerrarTurno(authentication.getName());
            return ResponseEntity.ok(turno);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Rutas para el Administrador
    @GetMapping("/todas")
    public ResponseEntity<?> listarTodasLasAsistencias() {
        return ResponseEntity.ok(asistenciaService.listarTodas());
    }

    @GetMapping("/horarios")
    public ResponseEntity<?> listarHorarios() {
        return ResponseEntity.ok(asistenciaService.listarHorarios());
    }

    static class HorarioRequest {
        public Long usuarioId;
        public List<String> diasSemana;
        public String horaInicio;
        public String horaFin;
    }

    @PostMapping("/horarios")
    public ResponseEntity<?> crearHorario(@RequestBody HorarioRequest payload) {
        try {
            asistenciaService.guardarHorariosMultiples(payload.usuarioId, payload.diasSemana, payload.horaInicio, payload.horaFin);
            return ResponseEntity.ok(Map.of("message", "Horarios guardados exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/horarios/{id}")
    public ResponseEntity<?> eliminarHorario(@PathVariable Long id) {
        try {
            asistenciaService.eliminarHorario(id);
            return ResponseEntity.ok(Map.of("message", "Horario eliminado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String formatTo12Hour(java.time.LocalTime time) {
        if (time == null) return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a");
        return time.format(formatter).toUpperCase();
    }

    @GetMapping("/horarios/pdf")
    public ResponseEntity<byte[]> exportarHorariosPdf() {
        try {
            List<HorarioTrabajador> horarios = asistenciaService.listarHorarios();
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate()); // Horizontal
            PdfWriter.getInstance(document, out);
            document.open();

            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new BaseColor(2, 132, 199)); // Azul moderno
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
            Font celdaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 4f});
            
            try {
                com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance("C:/Users/quica/Desktop/Proyecto gestion de citas/GestionDeCitasFront/public/logo_sysvet.png");
                logo.scaleToFit(80, 80);
                PdfPCell logoCell = new PdfPCell(logo);
                logoCell.setBorder(Rectangle.NO_BORDER);
                logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                headerTable.addCell(logoCell);
            } catch (Exception e) {
                PdfPCell empty = new PdfPCell(new Phrase(""));
                empty.setBorder(Rectangle.NO_BORDER);
                headerTable.addCell(empty);
            }

            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            
            Paragraph titulo = new Paragraph("Planilla Oficial de Horarios - PetyZoos", tituloFont);
            titulo.setAlignment(Element.ALIGN_RIGHT);
            
            Paragraph fecha = new Paragraph("Fecha de emisión: " + java.time.LocalDate.now().toString(), FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY));
            fecha.setAlignment(Element.ALIGN_RIGHT);
            
            titleCell.addElement(titulo);
            titleCell.addElement(fecha);
            headerTable.addCell(titleCell);
            headerTable.setSpacingAfter(30);

            document.add(headerTable);

            PdfPTable table = new PdfPTable(8); // 1 (Personal) + 7 (Dias)
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f});
            table.setSpacingAfter(15);
            
            String[] cabeceras = {"Personal", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
            for (String cabecera : cabeceras) {
                PdfPCell cell = new PdfPCell(new Phrase(cabecera, headerFont));
                cell.setBackgroundColor(new BaseColor(15, 23, 42)); // Slate-900 oscuro
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(10);
                table.addCell(cell);
            }

            // Agrupar horarios por empleado
            Map<Long, List<HorarioTrabajador>> horariosPorEmpleado = horarios.stream()
                .collect(Collectors.groupingBy(h -> h.getUsuario().getId()));

            for (List<HorarioTrabajador> empHorarios : horariosPorEmpleado.values()) {
                if (empHorarios.isEmpty()) continue;
                com.farmacia.sistemaWeb.entity.Usuario u = empHorarios.get(0).getUsuario();
                String personalStr = u.getNombres() + "\n(" + u.getRol().getNombre() + ")";
                
                PdfPCell personalCell = new PdfPCell(new Phrase(personalStr, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK)));
                personalCell.setPadding(10);
                personalCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(personalCell);

                String[] dias = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
                for (String dia : dias) {
                    HorarioTrabajador h = empHorarios.stream().filter(xh -> xh.getDiaSemana().name().equals(dia)).findFirst().orElse(null);
                    String timeStr = "";
                    if (h != null) {
                        timeStr = formatTo12Hour(h.getHoraInicio()) + "\n-\n" + formatTo12Hour(h.getHoraFin());
                    }
                    PdfPCell cell = new PdfPCell(new Phrase(timeStr, celdaFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(8);
                    
                    if (h != null) {
                        cell.setBackgroundColor(new BaseColor(224, 242, 254)); // Fondo azul claro para dias activos
                    }
                    
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Horarios_PetyZoos.pdf");

            return new ResponseEntity<>(out.toByteArray(), headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    static class ExportarAsistenciaRequest {
        public List<Long> ids;
        public List<Long> getIds() { return ids; }
        public void setIds(List<Long> ids) { this.ids = ids; }
    }

    private String formatTo12HourDateTime(java.time.LocalDateTime time) {
        if (time == null) return "--:--";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a");
        return time.format(formatter).toUpperCase();
    }

    @PostMapping("/diarias/pdf")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarAsistenciasDiariasPdf(@RequestBody ExportarAsistenciaRequest request) {
        try {
            if (request.getIds() == null || request.getIds().isEmpty()) {
                throw new RuntimeException("No hay IDs para exportar.");
            }

            List<AsistenciaDiaria> asistencias = asistenciaService.listarTodas().stream()
                .filter(a -> request.getIds().contains(a.getId()))
                .collect(Collectors.toList());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new BaseColor(2, 132, 199)); 
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
            Font celdaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 4f});
            
            try {
                com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance("C:/Users/quica/Desktop/Proyecto gestion de citas/GestionDeCitasFront/public/logo_sysvet.png");
                logo.scaleToFit(80, 80);
                PdfPCell logoCell = new PdfPCell(logo);
                logoCell.setBorder(Rectangle.NO_BORDER);
                logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                headerTable.addCell(logoCell);
            } catch (Exception e) {
                PdfPCell empty = new PdfPCell(new Phrase(""));
                empty.setBorder(Rectangle.NO_BORDER);
                headerTable.addCell(empty);
            }

            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            
            Paragraph titulo = new Paragraph("Reporte de Asistencia - PetyZoos", tituloFont);
            titulo.setAlignment(Element.ALIGN_RIGHT);
            
            Paragraph fecha = new Paragraph("Fecha de emisión: " + java.time.LocalDate.now().toString(), FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY));
            fecha.setAlignment(Element.ALIGN_RIGHT);
            
            titleCell.addElement(titulo);
            titleCell.addElement(fecha);
            headerTable.addCell(titleCell);
            headerTable.setSpacingAfter(30);
            document.add(headerTable);

            PdfPTable table = new PdfPTable(5); 
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 1f, 1.5f, 1.5f, 1f});
            table.setSpacingAfter(15);
            
            String[] cabeceras = {"Empleado", "Fecha", "Hora Entrada", "Hora Salida", "Estado"};
            for (String cabecera : cabeceras) {
                PdfPCell cell = new PdfPCell(new Phrase(cabecera, headerFont));
                cell.setBackgroundColor(new BaseColor(15, 23, 42)); 
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(10);
                table.addCell(cell);
            }

            for (AsistenciaDiaria a : asistencias) {
                String personalStr = a.getUsuario().getNombres() + "\n(" + a.getUsuario().getRol().getNombre() + ")";
                PdfPCell pCell = new PdfPCell(new Phrase(personalStr, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK)));
                pCell.setPadding(8);
                pCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(pCell);
                
                PdfPCell fCell = new PdfPCell(new Phrase(a.getFecha().toString(), celdaFont));
                fCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                fCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(fCell);
                
                PdfPCell eCell = new PdfPCell(new Phrase(formatTo12HourDateTime(a.getHoraApertura()), celdaFont));
                eCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                eCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(eCell);
                
                PdfPCell sCell = new PdfPCell(new Phrase(formatTo12HourDateTime(a.getHoraCierre()), celdaFont));
                sCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                sCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(sCell);
                
                PdfPCell estCell = new PdfPCell(new Phrase(a.getEstado().name(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, a.getEstado().name().equals("ABIERTO") ? new BaseColor(34, 197, 94) : BaseColor.GRAY)));
                estCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                estCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(estCell);
            }

            document.add(table);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Asistencias_PetyZoos.pdf");

            return new ResponseEntity<>(out.toByteArray(), headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
