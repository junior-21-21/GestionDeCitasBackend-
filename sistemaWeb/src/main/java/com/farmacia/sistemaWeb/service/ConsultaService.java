package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.ConsultaDTO;
import com.farmacia.sistemaWeb.entity.Cita;
import com.farmacia.sistemaWeb.entity.Consulta;
import com.farmacia.sistemaWeb.repository.CitaRepository;
import com.farmacia.sistemaWeb.repository.ConsultaRepository;
import com.farmacia.sistemaWeb.dto.ConsultaResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ConsultaService {

    @Autowired
    private CitaRepository citaRepository;
    
    @Autowired
    private ConsultaRepository consultaRepository;

    private String generarCodigoConsulta(LocalDate fecha) {
        String fechaStr = fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<Consulta> consultasHoy = consultaRepository.findByFecha(fecha);
        int maxSuffix = 0;
        for (Consulta c : consultasHoy) {
            try {
                String codigo = c.getCodigoConsulta();
                if (codigo != null && codigo.contains("-")) {
                    String suffixStr = codigo.substring(codigo.lastIndexOf("-") + 1);
                    int suffix = Integer.parseInt(suffixStr);
                    if (suffix > maxSuffix) {
                        maxSuffix = suffix;
                    }
                }
            } catch (Exception e) {
                // Ignorar si el formato no coincide
            }
        }
        return String.format("CON-%s-%03d", fechaStr, maxSuffix + 1);
    }

    public ConsultaResponseDTO mapToResponseDTO(Consulta c) {
        ConsultaResponseDTO dto = new ConsultaResponseDTO();
        dto.setCodigoConsulta(c.getCodigoConsulta());
        dto.setFecha(c.getFecha() != null ? c.getFecha().toString() : "");
        dto.setMotivo(c.getMotivo());
        dto.setDiagnostico(c.getDiagnostico());
        dto.setTratamiento(c.getTratamiento());
        dto.setNombrePaciente(c.getPaciente() != null ? c.getPaciente().getNombre() : "");
        dto.setNombreVeterinario(c.getVeterinario() != null ? c.getVeterinario().getNombres() : "");
        
        dto.setEstadoIngreso(c.getEstadoIngreso());
        dto.setEstadoSalida(c.getEstadoSalida());
        dto.setRequiereInternamiento(c.isRequiereInternamiento());
        dto.setMotivoInternamiento(c.getMotivoInternamiento());
        return dto;
    }

    private List<ConsultaResponseDTO> mapListToResponseDTO(List<Consulta> consultas) {
        return consultas.stream().map(this::mapToResponseDTO).toList();
    }

    @Transactional
    public ConsultaResponseDTO registrarConsulta(ConsultaDTO dto) {
        if (dto.getCitaCodigo() == null || dto.getCitaCodigo().isEmpty()) {
            throw new RuntimeException("El código de cita es obligatorio. Las consultas requieren cita previa.");
        }

        Cita cita = citaRepository.findById(dto.getCitaCodigo())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        // Validar que la cita no esté ya asociada a otra consulta
        if (consultaRepository.findByCitaCodigoCita(dto.getCitaCodigo()).isPresent()) {
            throw new RuntimeException("La cita " + dto.getCitaCodigo() + " ya tiene una consulta asociada");
        }

        Consulta consulta = new Consulta();
        consulta.setCodigoConsulta(generarCodigoConsulta(dto.getFecha()));
        consulta.setFecha(dto.getFecha());
        consulta.setMotivo(dto.getMotivo());
        consulta.setPeso(dto.getPeso());
        consulta.setObservaciones(dto.getObservaciones());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        
        consulta.setEstadoIngreso(dto.getEstadoIngreso());
        consulta.setEstadoSalida(dto.getEstadoSalida());
        consulta.setRequiereInternamiento(dto.isRequiereInternamiento());
        consulta.setMotivoInternamiento(dto.getMotivoInternamiento());
        
        // 3FN: Asignar la cita. Paciente y veterinario se obtienen transitivamente.
        consulta.setCita(cita);

        // Actualizar estado de la cita a REALIZADA
        cita.setEstado(Cita.EstadoCita.REALIZADA);
        citaRepository.save(cita);

        Consulta guardada = consultaRepository.save(consulta);
        return mapToResponseDTO(guardada);
    }

    public List<ConsultaResponseDTO> listarConsultas() {
        return mapListToResponseDTO(consultaRepository.findAll());
    }

    public List<ConsultaResponseDTO> obtenerHistorialPorPaciente(String codigoPaciente) {
        return mapListToResponseDTO(consultaRepository.findByCitaPacienteCodigoPacienteOrderByFechaDesc(codigoPaciente));
    }

    public byte[] generarHistorialPdf(String codigoPaciente) {
        List<Consulta> consultas = consultaRepository.findByCitaPacienteCodigoPacienteOrderByFechaDesc(codigoPaciente);
        if (consultas == null || consultas.isEmpty()) {
            throw new RuntimeException("No se encontraron consultas para el paciente: " + codigoPaciente);
        }

        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 54, 36);
            com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, out);
            document.open();

            com.itextpdf.text.Font titleFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 22, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font subtitleFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 12, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font headerFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font normalFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.BaseColor primaryColor = new com.itextpdf.text.BaseColor(41, 128, 185); // Blue
            com.itextpdf.text.BaseColor lightGray = new com.itextpdf.text.BaseColor(240, 240, 240);

            // Header
            com.itextpdf.text.pdf.PdfPTable headerTable = new com.itextpdf.text.pdf.PdfPTable(1);
            headerTable.setWidthPercentage(100);
            com.itextpdf.text.pdf.PdfPCell titleCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph("HISTORIAL CLÍNICO", titleFont));
            titleCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            titleCell.setPaddingBottom(20);
            headerTable.addCell(titleCell);
            document.add(headerTable);

            com.farmacia.sistemaWeb.entity.Paciente paciente = consultas.get(0).getCita().getPaciente();

            // Datos del Paciente Box
            com.itextpdf.text.pdf.PdfPTable patientTable = new com.itextpdf.text.pdf.PdfPTable(2);
            patientTable.setWidthPercentage(100);
            patientTable.setWidths(new float[]{1, 1});
            patientTable.setSpacingAfter(20);

            com.itextpdf.text.pdf.PdfPCell pTitle = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("INFORMACIÓN DEL PACIENTE", subtitleFont));
            pTitle.setColspan(2);
            pTitle.setBackgroundColor(primaryColor);
            pTitle.setPadding(8);
            pTitle.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            patientTable.addCell(pTitle);

            // Paciente datos
            com.itextpdf.text.pdf.PdfPCell cell1 = new com.itextpdf.text.pdf.PdfPCell();
            cell1.setPadding(8);
            cell1.setBackgroundColor(lightGray);
            cell1.setBorder(com.itextpdf.text.Rectangle.BOTTOM);
            cell1.setBorderColor(com.itextpdf.text.BaseColor.WHITE);
            cell1.addElement(new com.itextpdf.text.Paragraph("Nombre: " + paciente.getNombre(), headerFont));
            cell1.addElement(new com.itextpdf.text.Paragraph("Código: " + paciente.getCodigoPaciente(), normalFont));
            cell1.addElement(new com.itextpdf.text.Paragraph("Género: " + (paciente.getGenero() != null ? paciente.getGenero() : "N/D"), normalFont));
            patientTable.addCell(cell1);

            com.itextpdf.text.pdf.PdfPCell cell2 = new com.itextpdf.text.pdf.PdfPCell();
            cell2.setPadding(8);
            cell2.setBackgroundColor(lightGray);
            cell2.setBorder(com.itextpdf.text.Rectangle.BOTTOM);
            cell2.setBorderColor(com.itextpdf.text.BaseColor.WHITE);
            cell2.addElement(new com.itextpdf.text.Paragraph("Especie/Raza: " + paciente.getRaza().getEspecie().getNombre() + " - " + paciente.getRaza().getNombre(), normalFont));
            cell2.addElement(new com.itextpdf.text.Paragraph("Propietario: " + paciente.getCliente().getNombres() + " " + paciente.getCliente().getApellidos(), normalFont));
            cell2.addElement(new com.itextpdf.text.Paragraph("DNI: " + paciente.getCliente().getDni(), normalFont));
            patientTable.addCell(cell2);

            document.add(patientTable);

            // Title Atenciones
            com.itextpdf.text.Paragraph subtitle = new com.itextpdf.text.Paragraph("REGISTRO DE ATENCIONES MÉDICAS", com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 14, primaryColor));
            subtitle.setSpacingAfter(10);
            document.add(subtitle);

            // Table de Consultas
            for (Consulta c : consultas) {
                com.itextpdf.text.pdf.PdfPTable cTable = new com.itextpdf.text.pdf.PdfPTable(1);
                cTable.setWidthPercentage(100);
                cTable.setSpacingAfter(15);

                // Date Header
                com.itextpdf.text.pdf.PdfPCell dateCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Fecha: " + c.getFecha() + " | Atendido por: Dr. " + c.getCita().getVeterinario().getNombres(), headerFont));
                dateCell.setBackgroundColor(new com.itextpdf.text.BaseColor(230, 240, 245));
                dateCell.setPadding(6);
                dateCell.setBorderWidth(1);
                dateCell.setBorderColor(new com.itextpdf.text.BaseColor(200, 200, 200));
                cTable.addCell(dateCell);

                // Body
                com.itextpdf.text.pdf.PdfPCell bodyCell = new com.itextpdf.text.pdf.PdfPCell();
                bodyCell.setPadding(10);
                bodyCell.setBorderWidth(1);
                bodyCell.setBorderColor(new com.itextpdf.text.BaseColor(200, 200, 200));
                bodyCell.setBorderColorTop(com.itextpdf.text.BaseColor.WHITE);
                
                bodyCell.addElement(new com.itextpdf.text.Paragraph("Motivo de consulta:", headerFont));
                bodyCell.addElement(new com.itextpdf.text.Paragraph(c.getMotivo() != null ? c.getMotivo() : "-", normalFont));
                
                bodyCell.addElement(new com.itextpdf.text.Paragraph(" ", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 4))); // spacing
                
                bodyCell.addElement(new com.itextpdf.text.Paragraph("Diagnóstico:", headerFont));
                bodyCell.addElement(new com.itextpdf.text.Paragraph(c.getDiagnostico() != null && !c.getDiagnostico().isEmpty() ? c.getDiagnostico() : "No registrado", normalFont));

                bodyCell.addElement(new com.itextpdf.text.Paragraph(" ", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 4))); // spacing
                
                bodyCell.addElement(new com.itextpdf.text.Paragraph("Tratamiento:", headerFont));
                bodyCell.addElement(new com.itextpdf.text.Paragraph(c.getTratamiento() != null && !c.getTratamiento().isEmpty() ? c.getTratamiento() : "No registrado", normalFont));
                
                cTable.addCell(bodyCell);
                document.add(cTable);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el historial PDF", e);
        }
    }

    public List<ConsultaResponseDTO> listarConsultasHoy() {
        return mapListToResponseDTO(consultaRepository.findByFecha(LocalDate.now()));
    }

    public Consulta buscarPorCodigo(String codigoConsulta) {
        return consultaRepository.findById(codigoConsulta)
                .orElseThrow(() -> new RuntimeException("Consulta no encontrada"));
    }

    public List<ConsultaResponseDTO> buscarConsultasPorDniCliente(String dni) {
        return mapListToResponseDTO(consultaRepository.findByCitaPacienteClienteDni(dni));
    }

    @Transactional
    public Consulta actualizarConsulta(String codigoConsulta, ConsultaDTO dto) {
        Consulta consulta = buscarPorCodigo(codigoConsulta);

        consulta.setFecha(dto.getFecha());
        consulta.setMotivo(dto.getMotivo());
        consulta.setPeso(dto.getPeso());
        consulta.setObservaciones(dto.getObservaciones());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        
        consulta.setEstadoIngreso(dto.getEstadoIngreso());
        consulta.setEstadoSalida(dto.getEstadoSalida());
        consulta.setRequiereInternamiento(dto.isRequiereInternamiento());
        consulta.setMotivoInternamiento(dto.getMotivoInternamiento());

        if (dto.getCitaCodigo() != null && !dto.getCitaCodigo().isEmpty() &&
            !dto.getCitaCodigo().equals(consulta.getCita().getCodigoCita())) {
            
            // Revertir la cita anterior si es necesario, o solo asignar la nueva
            Cita cita = citaRepository.findById(dto.getCitaCodigo())
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
            consulta.setCita(cita);
        }

        return consultaRepository.save(consulta);
    }

    public void eliminarConsulta(String codigoConsulta) {
        Consulta consulta = buscarPorCodigo(codigoConsulta);
        consultaRepository.delete(consulta);
    }
}
