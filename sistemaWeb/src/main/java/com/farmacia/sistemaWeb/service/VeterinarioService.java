package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.VeterinarioDTO;
import com.farmacia.sistemaWeb.dto.VeterinarioResponseDTO;
import com.farmacia.sistemaWeb.entity.Especialidad;
import com.farmacia.sistemaWeb.entity.Veterinario;
import com.farmacia.sistemaWeb.repository.EspecialidadRepository;
import com.farmacia.sistemaWeb.repository.VeterinarioRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VeterinarioService {

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Value("${app.upload.dir:uploads/veterinarios}")
    private String uploadDir;

    public VeterinarioResponseDTO registrarVeterinario(VeterinarioDTO dto, MultipartFile foto, MultipartFile titulo) {
        Especialidad especialidad = especialidadRepository.findById(dto.getEspecialidadId())
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

        Veterinario v = new Veterinario();
        v.setDni(dto.getDni());
        v.setNombres(dto.getNombres());
        v.setCelular(dto.getCelular());
        v.setCorreo(dto.getCorreo());
        v.setEspecialidad(especialidad);

        if (foto != null && !foto.isEmpty()) {
            v.setFotoUrl(guardarArchivo(foto, "fotos"));
        }
        if (titulo != null && !titulo.isEmpty()) {
            v.setTituloUrl(guardarArchivo(titulo, "titulos"));
        }

        v = veterinarioRepository.save(v);
        return convertirAVeterinarioResponseDTO(v);
    }

    public List<VeterinarioResponseDTO> listarVeterinarios() {
        return veterinarioRepository.findAll().stream()
                .map(this::convertirAVeterinarioResponseDTO)
                .collect(Collectors.toList());
    }

    public VeterinarioResponseDTO obtenerPorDni(String dni) {
        Veterinario v = veterinarioRepository.findById(dni)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado con DNI: " + dni));
        return convertirAVeterinarioResponseDTO(v);
    }

    public void eliminar(String dni) {
        veterinarioRepository.deleteById(dni);
    }

    private VeterinarioResponseDTO convertirAVeterinarioResponseDTO(Veterinario v) {
        VeterinarioResponseDTO dto = new VeterinarioResponseDTO();
        dto.setDni(v.getDni());
        dto.setNombres(v.getNombres());
        dto.setCelular(v.getCelular());
        dto.setCorreo(v.getCorreo());
        dto.setFotoUrl(v.getFotoUrl());
        dto.setTituloUrl(v.getTituloUrl());
        if (v.getEspecialidad() != null) {
            dto.setEspecialidad(v.getEspecialidad().getNombre());
        }
        return dto;
    }

    public VeterinarioResponseDTO obtenerPorEmail(String email) {
        Veterinario v = veterinarioRepository.findByUsuarioEmail(email)
                .or(() -> veterinarioRepository.findByCorreo(email))
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado con email: " + email));
        return convertirAVeterinarioResponseDTO(v);
    }

    public VeterinarioResponseDTO actualizarVeterinario(String dni, VeterinarioDTO dto, MultipartFile foto,
            MultipartFile titulo) {
        Veterinario veterinario = veterinarioRepository.findById(dni)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado con DNI: " + dni));

        Especialidad especialidad = especialidadRepository.findById(dto.getEspecialidadId())
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

        veterinario.setNombres(dto.getNombres());
        veterinario.setCelular(dto.getCelular());
        veterinario.setCorreo(dto.getCorreo());
        veterinario.setEspecialidad(especialidad);

        if (foto != null && !foto.isEmpty()) {
            veterinario.setFotoUrl(guardarArchivo(foto, "fotos"));
        }
        if (titulo != null && !titulo.isEmpty()) {
            veterinario.setTituloUrl(guardarArchivo(titulo, "titulos"));
        }

        veterinario = veterinarioRepository.save(veterinario);
        return convertirAVeterinarioResponseDTO(veterinario);
    }

    private String guardarArchivo(MultipartFile archivo, String subcarpeta) {
        try {
            Path dirPath = Paths.get(uploadDir, subcarpeta);
            Files.createDirectories(dirPath);
            String originalFilename = archivo.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = dirPath.resolve(filename);
            Files.copy(archivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return subcarpeta + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar archivo: " + e.getMessage());
        }
    }

    public Path getUploadPath(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    public byte[] exportarExcel() {
        List<Veterinario> lista = veterinarioRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Veterinarios");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            String[] headers = { "DNI", "Nombres", "Celular", "Correo", "Especialidad" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Veterinario v : lista) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(v.getDni() != null ? v.getDni() : "");
                row.createCell(1).setCellValue(v.getNombres() != null ? v.getNombres() : "");
                row.createCell(2).setCellValue(v.getCelular() != null ? v.getCelular() : "");
                row.createCell(3).setCellValue(v.getCorreo() != null ? v.getCorreo() : "");
                row.createCell(4).setCellValue(v.getEspecialidad() != null ? v.getEspecialidad().getNombre() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage());
        }
    }
}
