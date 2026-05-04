package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.VeterinarioDTO;
import com.farmacia.sistemaWeb.dto.VeterinarioResponseDTO;
import com.farmacia.sistemaWeb.service.VeterinarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioController {

    @Autowired
    private VeterinarioService veterinarioService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrar(
            @RequestPart("datos") VeterinarioDTO dto,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @RequestPart(value = "titulo", required = false) MultipartFile titulo) {
        try {
            VeterinarioResponseDTO respuesta = veterinarioService.registrarVeterinario(dto, foto, titulo);
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel() {
        try {
            byte[] excelData = veterinarioService.exportarExcel();
            return ResponseEntity.ok()
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"veterinarios.xlsx\"")
                    .body(excelData);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<VeterinarioResponseDTO>> listar() {
        return ResponseEntity.ok(veterinarioService.listarVeterinarios());
    }

    @GetMapping("/por-email/{email}")
    public ResponseEntity<?> obtenerPorEmail(@PathVariable String email) {
        try {
            return ResponseEntity.ok(veterinarioService.obtenerPorEmail(email));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{dni}")
    public ResponseEntity<?> obtener(@PathVariable String dni) {
        try {
            return ResponseEntity.ok(veterinarioService.obtenerPorDni(dni));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{dni}")
    public ResponseEntity<?> eliminar(@PathVariable String dni) {
        veterinarioService.eliminar(dni);
        return ResponseEntity.ok("Veterinario eliminado");
    }

    @PutMapping(value = "/{dni}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> actualizar(
            @PathVariable String dni,
            @RequestPart("datos") VeterinarioDTO dto,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @RequestPart(value = "titulo", required = false) MultipartFile titulo) {
        try {
            VeterinarioResponseDTO actualizado = veterinarioService.actualizarVeterinario(dni, dto, foto, titulo);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/archivos/{subcarpeta}/{filename:.+}")
    public ResponseEntity<Resource> servirArchivo(
            @PathVariable String subcarpeta,
            @PathVariable String filename) {
        try {
            Path filePath = veterinarioService.getUploadPath(subcarpeta + "/" + filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
