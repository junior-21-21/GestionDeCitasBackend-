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
    public ResponseEntity<?> subirFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file,
            org.springframework.security.core.Authentication authentication) {
        try {
            Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Solo el propio usuario o un ADMIN pueden cambiar la foto; antes cualquiera con sesión podía.
            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            if (!esAdmin && !usuario.getEmail().equalsIgnoreCase(authentication.getName())) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                        .body("No tiene permisos para modificar este usuario.");
            }
            
            Path directory = Paths.get(uploadDir);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            
            // Solo imágenes reales (extensión + contenido); antes se aceptaba cualquier extensión
            String ext = ArchivoValidator.validar(file, ArchivoValidator.IMAGENES);
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
