package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.util.ArchivoValidator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import com.farmacia.sistemaWeb.entity.Paciente;
import com.farmacia.sistemaWeb.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pacientes/foto")
@RequiredArgsConstructor
public class PacienteFotoController {

    private final PacienteRepository pacienteRepository;
    private final String uploadDir = "uploads/pacientes/";

    @PostMapping("/{codigoPaciente}")
    public ResponseEntity<?> subirFoto(@PathVariable String codigoPaciente, @RequestParam("file") MultipartFile file) {
        try {
            Paciente paciente = pacienteRepository.findById(codigoPaciente)
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
            Path directory = Paths.get(uploadDir);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            
            // Solo imágenes reales (extensión + contenido); antes se aceptaba cualquier extensión
            String ext = ArchivoValidator.validar(file, ArchivoValidator.IMAGENES);
            
            String filename = "paciente_" + codigoPaciente + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
            Path filePath = directory.resolve(filename);
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String fileUrl = "/api/pacientes/foto/img/" + filename;
            paciente.setFotoUrl(fileUrl);
            pacienteRepository.save(paciente);
            
            return ResponseEntity.ok().body("{\"url\": \"" + fileUrl + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @GetMapping("/img/{filename}")
    public ResponseEntity<Resource> obtenerFoto(@PathVariable String filename) {
        try {
            Path file = ArchivoValidator.resolverSeguro(Paths.get(uploadDir), filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if (contentType == null) contentType = "application/octet-stream";
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .header("X-Content-Type-Options", "nosniff")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
