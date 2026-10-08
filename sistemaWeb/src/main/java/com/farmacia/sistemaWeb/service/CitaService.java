package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.CitaDTO;
import com.farmacia.sistemaWeb.dto.CitaResponseDTO;
import com.farmacia.sistemaWeb.entity.Cita;
import com.farmacia.sistemaWeb.entity.Paciente;
import com.farmacia.sistemaWeb.entity.Veterinario;
import com.farmacia.sistemaWeb.repository.CitaRepository;
import com.farmacia.sistemaWeb.repository.ConsultaRepository;
import com.farmacia.sistemaWeb.repository.PacienteRepository;
import com.farmacia.sistemaWeb.repository.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.ByteArrayOutputStream;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private VeterinarioRepository veterinarioRepository;
    @Autowired
    private ConsultaRepository consultaRepository;

    private String generarCodigoCita(LocalDate fecha) {
        String fechaStr = fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = citaRepository.countByFecha(fecha);
        return String.format("CIT-%s-%03d", fechaStr, count + 1);
    }

    public Cita registrarCita(CitaDTO dto) {
        if (dto.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException("No se pueden registrar citas en fechas pasadas.");
        }

        validarTraslape(dto.getVeterinarioDni(), dto.getFecha(), dto.getHora(), dto.getDuracionMinutos(), null);

        Paciente paciente = pacienteRepository.findById(dto.getPacienteCodigo())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Veterinario vet = veterinarioRepository.findById(dto.getVeterinarioDni())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        Cita c = new Cita();
        c.setCodigoCita(generarCodigoCita(dto.getFecha()));
        c.setFecha(dto.getFecha());
        c.setHora(dto.getHora());
        c.setMotivo(dto.getMotivo());
        c.setDuracionMinutos(dto.getDuracionMinutos() != null ? dto.getDuracionMinutos() : 30);
        c.setEstado(Cita.EstadoCita.PENDIENTE);
        c.setPaciente(paciente);
        c.setVeterinario(vet);

        return citaRepository.save(c);
    }

    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    public List<Cita> listarPorEstado(Cita.EstadoCita estado) {
        return citaRepository.findByEstado(estado);
    }

    public List<Cita> listarPorVeterinario(String vetDni) {
        return citaRepository.findByVeterinarioDni(vetDni);
    }

    public Cita cambiarEstado(String codigoCita, Cita.EstadoCita estado) {
        Cita c = citaRepository.findById(codigoCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (Cita.EstadoCita.REALIZADA.equals(c.getEstado()) && !Cita.EstadoCita.REALIZADA.equals(estado)) {
            throw new RuntimeException("No se puede modificar una cita que ya ha sido atendida.");
        }

        c.setEstado(estado);
        return citaRepository.save(c);
    }

    public List<CitaResponseDTO> listarDTO() {
        return citaRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public List<CitaResponseDTO> listarDTOPorVeterinario(String vetDni) {
        return citaRepository.findByVeterinarioDni(vetDni).stream().map(this::mapToDTO).toList();
    }

    private CitaResponseDTO mapToDTO(Cita c) {
        CitaResponseDTO dto = new CitaResponseDTO();
        dto.setCodigoCita(c.getCodigoCita());
        dto.setFecha(c.getFecha());
        dto.setHora(c.getHora());
        dto.setMotivo(c.getMotivo());
        dto.setEstado(c.getEstado() != null ? c.getEstado().name() : "");
        dto.setNombrePaciente(c.getPaciente().getNombre());
        dto.setNombreVeterinario(c.getVeterinario().getNombres());
        dto.setPacienteCodigo(c.getPaciente().getCodigoPaciente());
        dto.setVeterinarioDni(c.getVeterinario().getDni());
        dto.setDuracionMinutos(c.getDuracionMinutos());

        // Buscar consulta asociada para obtener el código
        consultaRepository.findByCitaCodigoCita(c.getCodigoCita())
                .ifPresent(consulta -> dto.setCodigoConsulta(consulta.getCodigoConsulta()));

        return dto;
    }

    public Cita editarCita(String codigoCita, CitaDTO dto) {
        Cita cita = citaRepository.findById(codigoCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (Cita.EstadoCita.REALIZADA.equals(cita.getEstado())) {
            throw new RuntimeException("No se puede editar una cita atendida.");
        }

        if (dto.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException("La nueva fecha no puede ser en el pasado.");
        }

        validarTraslape(dto.getVeterinarioDni(), dto.getFecha(), dto.getHora(), dto.getDuracionMinutos(), codigoCita);

        Paciente paciente = pacienteRepository.findById(dto.getPacienteCodigo())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Veterinario vet = veterinarioRepository.findById(dto.getVeterinarioDni())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());
        cita.setMotivo(dto.getMotivo());
        cita.setDuracionMinutos(dto.getDuracionMinutos() != null ? dto.getDuracionMinutos() : 30);
        cita.setPaciente(paciente);
        cita.setVeterinario(vet);
        return citaRepository.save(cita);
    }

    public void eliminar(String codigoCita) {
        citaRepository.deleteById(codigoCita);
    }

    public CitaResponseDTO obtenerPorCodigoDTO(String codigoCita) {
        Cita c = citaRepository.findById(codigoCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        return mapToDTO(c);
    }

    private void validarTraslape(String veterinarioDni, java.time.LocalDate fecha, java.time.LocalTime nuevaHoraInicio,
            Integer duracionMinutos, String citaCodigoExcluir) {
        if (duracionMinutos == null)
            duracionMinutos = 30;

        java.time.LocalTime nuevaHoraFin = nuevaHoraInicio.plusMinutes(duracionMinutos);
        List<Cita> citasDelDia = citaRepository.findByVeterinarioDniAndFecha(veterinarioDni, fecha);

        for (Cita existente : citasDelDia) {
            if (citaCodigoExcluir != null && existente.getCodigoCita().equals(citaCodigoExcluir))
                continue;
            if (Cita.EstadoCita.CANCELADA.equals(existente.getEstado()))
                continue;

            java.time.LocalTime extInicio = existente.getHora();
            Integer extDuracion = existente.getDuracionMinutos() != null ? existente.getDuracionMinutos() : 30;
            java.time.LocalTime extFin = extInicio.plusMinutes(extDuracion);

            if (nuevaHoraInicio.isBefore(extFin) && nuevaHoraFin.isAfter(extInicio)) {
                throw new RuntimeException(
                        "El horario seleccionado se cruza con otra cita (" + extInicio + " - " + extFin + ")");
            }
        }
    }

    public byte[] generarComprobantePdf(String codigoCita) {
        Cita cita = citaRepository.findById(codigoCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Formato Ticket (Ancho: ~80mm = 226 puntos, Alto dinámico o fijo)
            Rectangle ticketSize = new Rectangle(250, 600);
            Document document = new Document(ticketSize, 10, 10, 15, 15);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // Fuentes para Ticket (estilo monospace o sans-serif simple)
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font bodyFont = FontFactory.getFont(FontFactory.COURIER, 9, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 9, BaseColor.BLACK);
            Font titleFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 10, BaseColor.BLACK);

            // 1. Logo
            try {
                org.springframework.core.io.ClassPathResource imgFile = new org.springframework.core.io.ClassPathResource("static/images/logo_sysvet.png");
                Image logo = Image.getInstance(imgFile.getURL());
                logo.scaleToFit(80, 80);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                Paragraph fallbackLogo = new Paragraph("PETYZOOS", headerFont);
                fallbackLogo.setAlignment(Element.ALIGN_CENTER);
                document.add(fallbackLogo);
            }

            // 2. Encabezado de la Veterinaria
            Paragraph header = new Paragraph("VETERINARIA PETYZOOS\nRUC: 20123456789\nAv. Las Mascotas 123, Lima\nTel: 01-234-5678", bodyFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            String separator = "--------------------------------------";
            Paragraph sep = new Paragraph(separator, bodyFont);
            sep.setAlignment(Element.ALIGN_CENTER);
            document.add(sep);

            // 3. Título del Ticket
            Paragraph title = new Paragraph("TICKET DE CITA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Paragraph numCita = new Paragraph("N° " + cita.getCodigoCita(), boldFont);
            numCita.setAlignment(Element.ALIGN_CENTER);
            document.add(numCita);

            document.add(sep);

            // 4. Datos de Cita
            document.add(new Paragraph(String.format("%-11s: %s", "FECHA", cita.getFecha().toString()), bodyFont));
            document.add(new Paragraph(String.format("%-11s: %s", "HORA", cita.getHora().toString()), bodyFont));
            document.add(new Paragraph(String.format("%-11s: %s", "ESTADO", cita.getEstado().name()), bodyFont));
            document.add(new Paragraph(String.format("%-11s: %s %s", "VETERINARIO", cita.getVeterinario().getNombres(), cita.getVeterinario().getApellidos()), bodyFont));

            document.add(sep);

            // 5. Datos de Paciente
            document.add(new Paragraph(String.format("%-11s: %s", "PACIENTE", cita.getPaciente().getNombre()), bodyFont));
            document.add(new Paragraph(String.format("%-11s: %s", "ESPECIE", cita.getPaciente().getRaza().getEspecie().getNombre()), bodyFont));
            document.add(new Paragraph(String.format("%-11s: %s %s", "CLIENTE", cita.getPaciente().getCliente().getNombres(), cita.getPaciente().getCliente().getApellidos()), bodyFont));
            document.add(new Paragraph(String.format("%-11s: %s", "MOTIVO", cita.getMotivo()), bodyFont));

            document.add(sep);

            document.add(new Paragraph("\n"));

            // 6. Código de Barras (Usando iText Barcode)
            try {
                com.itextpdf.text.pdf.PdfContentByte cb = writer.getDirectContent();
                com.itextpdf.text.pdf.Barcode128 barcode = new com.itextpdf.text.pdf.Barcode128();
                barcode.setCode(cita.getCodigoCita());
                barcode.setCodeType(com.itextpdf.text.pdf.Barcode128.CODE128);
                Image code128Image = barcode.createImageWithBarcode(cb, BaseColor.BLACK, BaseColor.BLACK);
                code128Image.setAlignment(Element.ALIGN_CENTER);
                code128Image.scalePercent(120);
                document.add(code128Image);
            } catch (Exception e) {
                // Ignore barcode error
            }

            document.add(new Paragraph("\n"));
            Paragraph footer = new Paragraph("¡Gracias por confiar en nosotros!\nPor favor asista 10 min antes.", bodyFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            System.err.println("Error generando el comprobante PDF (Ticket): " + ex.getMessage());
            throw new RuntimeException("Error al generar el ticket PDF", ex);
        }
    }
}