package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.UsuarioDTO;
import com.farmacia.sistemaWeb.entity.Usuario;
import com.farmacia.sistemaWeb.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/admin")
    public ResponseEntity<?> registrarAdmin(@RequestBody UsuarioDTO dto) {
        try {
            Usuario usuario = usuarioService.registrarPrimerUsuario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/vendedor")
    public ResponseEntity<?> registrarVendedor(@RequestBody UsuarioDTO dto) {
        try {
            Usuario vendedor = usuarioService.registrarVendedor(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(vendedor);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // --- ENDPOINTS CRUD ---

    @org.springframework.web.bind.annotation.GetMapping
    public java.util.List<Usuario> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@org.springframework.web.bind.annotation.PathVariable Long id,
            @RequestBody UsuarioDTO dto) {
        try {
            Usuario actualizado = usuarioService.actualizarUsuario(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@org.springframework.web.bind.annotation.PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/password")
    public ResponseEntity<?> cambiarPassword(@org.springframework.web.bind.annotation.PathVariable Long id,
            @RequestBody java.util.Map<String, String> payload) {
        try {
            String newPassword = payload.get("password");
            if (newPassword == null || newPassword.isBlank()) {
                throw new RuntimeException("La contraseña es obligatoria");
            }
            usuarioService.cambiarPassword(id, newPassword);
            return ResponseEntity.ok("Contraseña actualizada");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
