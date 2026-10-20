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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.farmacia.sistemaWeb.util.GlobalExceptionHandler.BusinessRuleException;
import com.farmacia.sistemaWeb.util.GlobalExceptionHandler.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

    /** Zona horaria de la clínica: evita que "hoy" cambie según la zona del servidor (Docker suele usar UTC). */
    public static final ZoneId ZONA_CLINICA = ZoneId.of("America/Lima");
    private static final int DURACION_POR_DEFECTO = 30;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @Value("${app.citas.hora-apertura:08:00}")
    private String horaApertura;

    @Value("${app.citas.hora-cierre:20:00}")
    private String horaCierre;

    /**
     * Genera el siguiente código libre del día. Antes se usaba count+1, que tras borrar una cita
     * devolvía un código ya existente y save() sobrescribía esa otra cita.
     */
    private String generarCodigoCita(LocalDate fecha) {
        String prefijo = "CIT-" + fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        int max = 0;
        for (Cita c : citaRepository.findByCodigoCitaStartingWith(prefijo)) {
            try {
                max = Math.max(max, Integer.parseInt(c.getCodigoCita().substring(prefijo.length())));
            } catch (NumberFormatException ignored) {
                // código con formato no estándar: no cuenta para el correlativo
            }
        }
        String codigo;
        do {
            codigo = String.format("%s%03d", prefijo, ++max);
        } while (citaRepository.existsById(codigo));
        return codigo;
    }

    private int duracionDe(Integer duracionMinutos) {
        return duracionMinutos != null ? duracionMinutos : DURACION_POR_DEFECTO;
    }

    /** Valida que la cita sea futura, dure lo razonable y quede dentro del horario de atención. */
    private void validarFechaHora(LocalDate fecha, LocalTime hora, int duracion) {
        if (duracion < 5 || duracion > 240) {
            throw new BusinessRuleException("La duración de la cita debe estar entre 5 y 240 minutos.");
        }
        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now(ZONA_CLINICA))) {
            throw new BusinessRuleException("No se pueden registrar citas en fechas u horas pasadas.");
        }
        LocalTime apertura = LocalTime.parse(horaApertura);
        LocalTime cierre = LocalTime.parse(horaCierre);
        // Si la cita cruza la medianoche, plusMinutes "da la vuelta" y el fin queda antes del inicio.
        LocalTime fin = hora.plusMinutes(duracion);
        if (hora.isBefore(apertura) || fin.isBefore(hora) || fin.isAfter(cierre)) {
            throw new BusinessRuleException(
                    "La cita debe estar dentro del horario de atención (" + apertura + " - " + cierre + ").");
        }
    }

    @Transactional
    public Cita registrarCita(CitaDTO dto) {
        int duracion = duracionDe(dto.getDuracionMinutos());
        validarFechaHora(dto.getFecha(), dto.getHora(), duracion);

        // Bloqueo de veterinario y paciente: dos peticiones simultáneas no pueden pasar ambas la validación de traslape
        Veterinario vet = veterinarioRepository.findByDniParaActualizar(dto.getVeterinarioDni())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado"));
        Paciente paciente = pacienteRepository.findByCodigoParaActualizar(dto.getPacienteCodigo())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        validarVeterinarioActivo(vet);
        validarTraslape(vet.getDni(), paciente.getCodigoPaciente(), dto.getFecha(), dto.getHora(), duracion, null);

        Cita c = new Cita();
        c.setCodigoCita(generarCodigoCita(dto.getFecha()));
        c.setFecha(dto.getFecha());
        c.setHora(dto.getHora());
        c.setMotivo(dto.getMotivo().trim());
        c.setDuracionMinutos(duracion);
        c.setEstado(Cita.EstadoCita.PENDIENTE);
        c.setPaciente(paciente);
        c.setVeterinario(vet);

        // persist (INSERT) y no save: con id asignado, save hace merge y si dos recepcionistas
        // generaban el mismo codigo a la vez, la segunda cita SOBRESCRIBIA a la primera sin error.
        entityManager.persist(c);
        entityManager.flush();
        return c;
    }

    private void validarVeterinarioActivo(Veterinario vet) {
        if (vet.getUsuario() != null && !vet.getUsuario().isHabilitada()) {
            throw new BusinessRuleException("El veterinario seleccionado tiene la cuenta inhabilitada.");
        }
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

    @Transactional
    public Cita cambiarEstado(String codigoCita, Cita.EstadoCita estado) {
        Cita c = citaRepository.findById(codigoCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        Cita.EstadoCita actual = c.getEstado();
        if (estado == actual) {
            return c;
        }
        if (Cita.EstadoCita.REALIZADA.equals(actual)) {
            throw new BusinessRuleException("No se puede modificar una cita que ya ha sido atendida.");
        }
        if (Cita.EstadoCita.REALIZADA.equals(estado)) {
            // Solo registrar la consulta marca la cita como realizada; antes se podia marcar una
            // cita futura como atendida sin consulta y quedaba bloqueada para siempre.
            throw new BusinessRuleException("La cita se marca como realizada al registrar su consulta.");
        }
        if (Cita.EstadoCita.CANCELADA.equals(actual) && Cita.EstadoCita.REALIZADA.equals(estado)) {
            throw new BusinessRuleException("Una cita cancelada no puede marcarse como realizada.");
        }
        if (Cita.EstadoCita.CANCELADA.equals(actual) && Cita.EstadoCita.PENDIENTE.equals(estado)) {
            // Reactivar: el horario puede haber sido ocupado por otra cita mientras estaba cancelada
            int duracion = duracionDe(c.getDuracionMinutos());
            validarFechaHora(c.getFecha(), c.getHora(), duracion);
            veterinarioRepository.findByDniParaActualizar(c.getVeterinario().getDni());
            pacienteRepository.findByCodigoParaActualizar(c.getPaciente().getCodigoPaciente());
            validarTraslape(c.getVeterinario().getDni(), c.getPaciente().getCodigoPaciente(),
                    c.getFecha(), c.getHora(), duracion, codigoCita);
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

    @Transactional
    public Cita editarCita(String codigoCita, CitaDTO dto) {
        Cita cita = citaRepository.findById(codigoCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        if (Cita.EstadoCita.REALIZADA.equals(cita.getEstado())) {
            throw new BusinessRuleException("No se puede editar una cita atendida.");
        }
        if (Cita.EstadoCita.CANCELADA.equals(cita.getEstado())) {
            throw new BusinessRuleException("No se puede editar una cita cancelada. Reactívela primero.");
        }

        int duracion = duracionDe(dto.getDuracionMinutos());
        validarFechaHora(dto.getFecha(), dto.getHora(), duracion);

        Veterinario vet = veterinarioRepository.findByDniParaActualizar(dto.getVeterinarioDni())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado"));
        Paciente paciente = pacienteRepository.findByCodigoParaActualizar(dto.getPacienteCodigo())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        if (!vet.getDni().equals(cita.getVeterinario().getDni())) {
            validarVeterinarioActivo(vet);
        }
        validarTraslape(vet.getDni(), paciente.getCodigoPaciente(), dto.getFecha(), dto.getHora(), duracion, codigoCita);

        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());
        cita.setMotivo(dto.getMotivo().trim());
        cita.setDuracionMinutos(duracion);
        cita.setPaciente(paciente);
        cita.setVeterinario(vet);
        return citaRepository.save(cita);
    }

    @Transactional
    public void eliminar(String codigoCita) {
        Cita cita = citaRepository.findById(codigoCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        if (Cita.EstadoCita.REALIZADA.equals(cita.getEstado())
                || consultaRepository.findByCitaCodigoCita(codigoCita).isPresent()) {
            throw new BusinessRuleException("No se puede eliminar una cita atendida o con consulta registrada.");
        }
        citaRepository.delete(cita);
    }

    public CitaResponseDTO obtenerPorCodigoDTO(String codigoCita) {
        Cita c = citaRepository.findById(codigoCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        return mapToDTO(c);
    }

    /** Comprueba que ni el veterinario ni la mascota tengan otra cita activa que se cruce con el horario. */
    private void validarTraslape(String veterinarioDni, String pacienteCodigo, LocalDate fecha,
            LocalTime nuevaHoraInicio, int duracionMinutos, String citaCodigoExcluir) {
        LocalTime nuevaHoraFin = nuevaHoraInicio.plusMinutes(duracionMinutos);

        for (Cita existente : citaRepository.findByVeterinarioDniAndFecha(veterinarioDni, fecha)) {
            if (seCruza(existente, nuevaHoraInicio, nuevaHoraFin, citaCodigoExcluir)) {
                throw new BusinessRuleException("El veterinario ya tiene una cita en ese horario ("
                        + existente.getHora() + " - " + existente.getHora().plusMinutes(duracionDe(existente.getDuracionMinutos())) + ")");
            }
        }
        for (Cita existente : citaRepository.findByPacienteCodigoPacienteAndFecha(pacienteCodigo, fecha)) {
            if (seCruza(existente, nuevaHoraInicio, nuevaHoraFin, citaCodigoExcluir)) {
                throw new BusinessRuleException("La mascota ya tiene otra cita en ese horario ("
                        + existente.getHora() + " - " + existente.getHora().plusMinutes(duracionDe(existente.getDuracionMinutos())) + ")");
            }
        }
    }

    private boolean seCruza(Cita existente, LocalTime inicio, LocalTime fin, String citaCodigoExcluir) {
        if (citaCodigoExcluir != null && existente.getCodigoCita().equals(citaCodigoExcluir))
            return false;
        if (Cita.EstadoCita.CANCELADA.equals(existente.getEstado()))
            return false;
        LocalTime extInicio = existente.getHora();
        LocalTime extFin = extInicio.plusMinutes(duracionDe(existente.getDuracionMinutos()));
        return inicio.isBefore(extFin) && fin.isAfter(extInicio);
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