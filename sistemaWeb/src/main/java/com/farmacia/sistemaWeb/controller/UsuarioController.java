package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.UsuarioDTO;
import com.farmacia.sistemaWeb.entity.Usuario;
import com.farmacia.sistemaWeb.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private org.springframework.core.env.Environment environment;

    /** Clave de un solo uso para crear el primer ADMIN fuera de dev (variable ADMIN_BOOTSTRAP_TOKEN). */
    @org.springframework.beans.factory.annotation.Value("${app.bootstrap.admin-token:}")
    private String bootstrapToken;

    @PostMapping("/admin")
    public ResponseEntity<?> registrarAdmin(@Valid @RequestBody UsuarioDTO dto,
            @RequestHeader(value = "X-Bootstrap-Token", required = false) String tokenRecibido) {
        // Ruta publica: sin esta clave, el primero que llegara a un servidor recien desplegado se
        // quedaba con la cuenta de administrador. En dev se permite sin clave para pruebas locales.
        boolean dev = environment.acceptsProfiles(org.springframework.core.env.Profiles.of("dev"));
        if (bootstrapToken == null || bootstrapToken.isBlank()) {
            if (!dev) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        "Defina ADMIN_BOOTSTRAP_TOKEN en el servidor y envielo en la cabecera X-Bootstrap-Token para crear el primer administrador.");
            }
        } else if (tokenRecibido == null || !java.security.MessageDigest.isEqual(
                bootstrapToken.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                tokenRecibido.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Clave de arranque incorrecta.");
        }
        try {
            Usuario usuario = usuarioService.registrarPrimerUsuario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @PostMapping("/vendedor")
    public ResponseEntity<?> registrarVendedor(@Valid @RequestBody UsuarioDTO dto) {
        try {
            Usuario vendedor = usuarioService.registrarVendedor(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(vendedor);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody UsuarioDTO dto) {
        try {
            Usuario usuario = usuarioService.crearUsuarioConRoles(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    // --- ENDPOINTS CRUD ---

    @GetMapping
    public java.util.List<Usuario> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDTO dto) {
        try {
            Usuario actualizado = usuarioService.actualizarUsuario(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (com.farmacia.sistemaWeb.util.GlobalExceptionHandler.DuplicateResourceException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                    .body(java.util.Map.of("mensaje", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> cambiarPassword(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String newPassword = payload.get("password");
            if (newPassword == null || newPassword.isBlank()) {
                throw new RuntimeException("La contraseña es obligatoria");
            }
            usuarioService.cambiarPassword(id, newPassword);
            return ResponseEntity.ok("Contraseña actualizada");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @PutMapping("/{id}/imagen")
    public ResponseEntity<?> actualizarImagen(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String imagen = payload.get("imagen");
            usuarioService.actualizarImagen(id, imagen);
            return ResponseEntity.ok("Imagen actualizada");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @GetMapping("/{id}/imagen")
    public ResponseEntity<?> obtenerImagen(@PathVariable Long id) {
        try {
            String imagen = usuarioService.obtenerImagen(id);
            Map<String, String> response = new HashMap<>();
            response.put("imagen", imagen);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @PutMapping("/{id}/desbloquear")
    public ResponseEntity<?> desbloquearCuenta(@PathVariable Long id) {
        try {
            usuarioService.desbloquearCuenta(id);
            return ResponseEntity.ok("Cuenta desbloqueada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoCuenta(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        try {
            Boolean estado = payload.get("estado");
            if (estado == null) {
                return ResponseEntity.badRequest().body("El estado es obligatorio");
            }
            usuarioService.cambiarEstadoCuenta(id, estado);
            return ResponseEntity.ok("Estado actualizado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
