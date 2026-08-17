package com.farmacia.sistemaWeb.controller;

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
import com.farmacia.sistemaWeb.entity.Usuario;
import com.farmacia.sistemaWeb.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios/foto")
@RequiredArgsConstructor
public class UsuarioFotoController {

    private final UsuarioRepository usuarioRepository;
    private final String uploadDir = "uploads/usuarios/";

    @PostMapping("/{id}")
    public ResponseEntity<?> subirFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            Path directory = Paths.get(uploadDir);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            
            String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String filename = "user_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
            Path filePath = directory.resolve(filename);
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String fileUrl = "/api/usuarios/foto/img/" + filename;
            usuario.setFotoUrl(fileUrl);
            usuarioRepository.save(usuario);
            
            return ResponseEntity.ok().body("{\"url\": \"" + fileUrl + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/img/{filename}")
    public ResponseEntity<Resource> obtenerFoto(@PathVariable String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if (contentType == null) contentType = "application/octet-stream";
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
