package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.PacienteDTO;
import com.farmacia.sistemaWeb.dto.PacienteResponseDTO;
import com.farmacia.sistemaWeb.entity.Cliente;
import com.farmacia.sistemaWeb.entity.Paciente;
import com.farmacia.sistemaWeb.entity.Raza;
import com.farmacia.sistemaWeb.repository.ClienteRepository;
import com.farmacia.sistemaWeb.repository.PacienteRepository;
import com.farmacia.sistemaWeb.repository.RazaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RazaRepository razaRepository;

    private String generarCodigoPaciente(String nombre) {
        String prefijo = nombre.toUpperCase().replaceAll("[^A-Z]", "");
        if (prefijo.length() > 4)
            prefijo = prefijo.substring(0, 4);
        if (prefijo.isEmpty())
            prefijo = "PAC";

        long count = pacienteRepository
                .countByNombreStartingWithIgnoreCase(nombre.substring(0, Math.min(3, nombre.length())));
        return String.format("PAC-%s-%03d", prefijo, count + 1);
    }

    public PacienteResponseDTO registrarPaciente(PacienteDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteDni())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con DNI: " + dto.getClienteDni()));

        Raza raza = razaRepository.findById(dto.getRazaId())
                .orElseThrow(() -> new RuntimeException("Raza no encontrada con ID: " + dto.getRazaId()));

        Paciente paciente = new Paciente();
        paciente.setCodigoPaciente(generarCodigoPaciente(dto.getNombre()));
        paciente.setNombre(dto.getNombre());
        paciente.setRaza(raza);
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setPeso(dto.getPeso());
        paciente.setCliente(cliente);

        return mapToResponseDTO(pacienteRepository.save(paciente));
    }

    public List<PacienteResponseDTO> obtenerPacientesPorCliente(String clienteDni) {
        return pacienteRepository.findByClienteDni(clienteDni)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<PacienteResponseDTO> listarTodos() {
        return pacienteRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<Paciente> listarEntidades() {
        return pacienteRepository.findAll();
    }

    public PacienteResponseDTO obtenerPorCodigo(String codigoPaciente) {
        Paciente paciente = pacienteRepository.findById(codigoPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + codigoPaciente));
        return mapToResponseDTO(paciente);
    }

    private PacienteResponseDTO mapToResponseDTO(Paciente paciente) {
        PacienteResponseDTO dto = new PacienteResponseDTO();
        dto.setCodigoPaciente(paciente.getCodigoPaciente());
        dto.setNombre(paciente.getNombre());

        if (paciente.getRaza() != null) {
            dto.setRaza(paciente.getRaza().getNombre());
            dto.setRazaId(paciente.getRaza().getId());
            if (paciente.getRaza().getEspecie() != null) {
                dto.setEspecie(paciente.getRaza().getEspecie().getNombre());
                dto.setEspecieId(paciente.getRaza().getEspecie().getId());
            }
        }

        dto.setFechaNacimiento(paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento().toString() : null);
        dto.setEdadCalculada(paciente.getEdadCalculada() != null ? paciente.getEdadCalculada() : 0);
        dto.setPeso(paciente.getPeso());
        dto.setClienteDni(paciente.getCliente().getDni());
        dto.setClienteNombreCompleto(
                paciente.getCliente().getNombres() + " " + paciente.getCliente().getApellidos());
        dto.setFotoUrl(paciente.getFotoUrl());
        return dto;
    }

    public void eliminarPaciente(String codigoPaciente) {
        pacienteRepository.deleteById(codigoPaciente);
    }

    public PacienteResponseDTO actualizarPaciente(String codigoPaciente, PacienteDTO dto) {
        Paciente paciente = pacienteRepository.findById(codigoPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + codigoPaciente));

        Cliente cliente = clienteRepository.findById(dto.getClienteDni())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con DNI: " + dto.getClienteDni()));

        Raza raza = razaRepository.findById(dto.getRazaId())
                .orElseThrow(() -> new RuntimeException("Raza no encontrada con ID: " + dto.getRazaId()));

        paciente.setNombre(dto.getNombre());
        paciente.setRaza(raza);
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setPeso(dto.getPeso());
        paciente.setCliente(cliente);

        return mapToResponseDTO(pacienteRepository.save(paciente));
    }

    public List<PacienteResponseDTO> buscarPacientesPorNombre(String nombre) {
        return pacienteRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<PacienteResponseDTO> buscarPacientesPorDni(String dni) {
        return pacienteRepository.findByClienteDni(dni)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public byte[] generarCredencialPdf(String codigoPaciente) {
        Paciente paciente = pacienteRepository.findById(codigoPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        String especieNombre = paciente.getRaza() != null && paciente.getRaza().getEspecie() != null
                ? paciente.getRaza().getEspecie().getNombre() : "N/A";
        String razaNombre = paciente.getRaza() != null
                ? paciente.getRaza().getNombre() : "N/A";
                
        Integer edad = paciente.getEdadCalculada();
        String edadStr = edad != null ? edad + " años" : "N/A";

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.DARK_GRAY);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.GRAY);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font codeFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 18, BaseColor.BLUE);

            Paragraph clinicName = new Paragraph("CLÍNICA VETERINARIA VRAEM", headerFont);
            clinicName.setAlignment(Element.ALIGN_CENTER);
            document.add(clinicName);

            Paragraph clinicAddress = new Paragraph("Av. Principal 123 - Tel: 01-2345678", regularFont);
            clinicAddress.setAlignment(Element.ALIGN_CENTER);
            document.add(clinicAddress);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph(" "));

            Paragraph title = new Paragraph("CREDENCIAL DE MASCOTA", subHeaderFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            Paragraph codeParagraph = new Paragraph("CÓDIGO ÚNICO: " + paciente.getCodigoPaciente(), codeFont);
            codeParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(codeParagraph);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Utilice este código para buscar el historial clínico.", regularFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            PdfPCell cell;
            cell = new PdfPCell(new Phrase("DATOS DE LA MASCOTA", subHeaderFont));
            cell.setColspan(2);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(8f);
            table.addCell(cell);

            table.addCell(new Phrase("Nombre:", headerFont));
            table.addCell(new Phrase(paciente.getNombre(), regularFont));

            table.addCell(new Phrase("Especie:", headerFont));
            table.addCell(new Phrase(especieNombre, regularFont));

            table.addCell(new Phrase("Raza:", headerFont));
            table.addCell(new Phrase(razaNombre, regularFont));

            table.addCell(new Phrase("Edad:", headerFont));
            table.addCell(new Phrase(edadStr, regularFont));

            table.addCell(new Phrase("Peso:", headerFont));
            table.addCell(new Phrase(paciente.getPeso() != null ? paciente.getPeso() + " kg" : "N/A", regularFont));

            cell = new PdfPCell(new Phrase("DATOS DEL PROPIETARIO", subHeaderFont));
            cell.setColspan(2);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(8f);
            table.addCell(cell);

            table.addCell(new Phrase("Nombre Completo:", headerFont));
            table.addCell(new Phrase(paciente.getCliente().getNombres() + " " + paciente.getCliente().getApellidos(),
                    regularFont));

            table.addCell(new Phrase("DNI / Identificación:", headerFont));
            table.addCell(new Phrase(paciente.getCliente().getDni(), regularFont));

            document.add(table);
            document.close();
        } catch (DocumentException ex) {
            System.err.println("Error generating PDF credential: " + ex.getMessage());
        }

        return out.toByteArray();
    }
}
