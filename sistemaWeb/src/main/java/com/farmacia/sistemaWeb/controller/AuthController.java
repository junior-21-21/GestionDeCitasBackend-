package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.LoginDTO;
import com.farmacia.sistemaWeb.dto.LoginResponse;
import com.farmacia.sistemaWeb.entity.Usuario;
import com.farmacia.sistemaWeb.repository.UsuarioRepository;
import com.farmacia.sistemaWeb.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private static final int MAX_INTENTOS = 5;

        private static final int BLOQUEO_MINUTOS = 15;

        // Hash de una contrasena aleatoria descartada; solo sirve para igualar tiempos de respuesta.
        private volatile String hashFicticio;

        @Autowired
        private AuthenticationManager authManager;

        @Autowired
        private JwtProvider jwtProvider;

        @Autowired
        private UsuarioRepository usuarioRepository;

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
                // Validar que se envien datos
                if (dto.getEmail() == null || dto.getEmail().isBlank() ||
                                dto.getPassword() == null || dto.getPassword().isBlank()) {
                        return ResponseEntity.badRequest().body(
                                        Map.of("error", "El correo y la contrasena son obligatorios"));
                }

                // Mientras no se conozca la contrasena, todas las respuestas son iguales: antes un
                // atacante podia saber que correos existen y cuales estaban bloqueados o inhabilitados.
                Optional<Usuario> optUsuario = usuarioRepository.findByEmail(dto.getEmail());
                if (optUsuario.isEmpty()) {
                        // Se compara igualmente contra un hash para que el tiempo de respuesta no delate el correo.
                        if (hashFicticio == null) {
                                hashFicticio = passwordEncoder.encode(java.util.UUID.randomUUID().toString());
                        }
                        passwordEncoder.matches(dto.getPassword(), hashFicticio);
                        return credencialesIncorrectas();
                }

                Usuario usuario = optUsuario.get();

                // El bloqueo por intentos fallidos dura BLOQUEO_MINUTOS. Antes era permanente y cualquiera
                // podia dejar sin acceso al ADMIN escribiendo mal su contrasena 5 veces.
                if (usuario.isCuentaBloqueada() && usuario.getFechaBloqueo() != null
                                && usuario.getFechaBloqueo().plusMinutes(BLOQUEO_MINUTOS).isBefore(LocalDateTime.now())) {
                        usuario.setCuentaBloqueada(false);
                        usuario.setIntentosFallidos(0);
                        usuario.setFechaBloqueo(null);
                        usuarioRepository.save(usuario);
                }

                if (!passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
                        if (!usuario.isCuentaBloqueada()) {
                                int intentos = usuario.getIntentosFallidos() + 1;
                                usuario.setIntentosFallidos(intentos);
                                if (intentos >= MAX_INTENTOS) {
                                        usuario.setCuentaBloqueada(true);
                                        usuario.setFechaBloqueo(LocalDateTime.now());
                                }
                                usuarioRepository.save(usuario);
                        }
                        return credencialesIncorrectas();
                }

                // Contrasena correcta: ya se puede informar el estado real de la cuenta.
                if (!usuario.isHabilitada()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                        Map.of(
                                                        "error", "CUENTA_INHABILITADA",
                                                        "mensaje",
                                                        "Su cuenta ha sido inhabilitada temporalmente por el administrador. Contactelo para más informacion."));
                }

                if (usuario.isCuentaBloqueada()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                        Map.of(
                                                        "error", "CUENTA_BLOQUEADA",
                                                        "mensaje",
                                                        "Su cuenta esta bloqueada por multiples intentos fallidos. Intente de nuevo en "
                                                                        + BLOQUEO_MINUTOS + " minutos o contacte al administrador.",
                                                        "intentos", MAX_INTENTOS));
                }

                Authentication auth = authManager.authenticate(
                                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

                // Login exitoso: resetear intentos fallidos
                usuario.setIntentosFallidos(0);
                usuarioRepository.save(usuario);

                String token = jwtProvider.generarToken(auth);

                return ResponseEntity.ok(new LoginResponse(
                                usuario.getId(),
                                usuario.getEmail(),
                                usuario.getNombres(),
                                usuario.getRol().getNombre().name(),
                                token));
        }

        private ResponseEntity<?> credencialesIncorrectas() {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                Map.of(
                                                "error", "CREDENCIALES_INCORRECTAS",
                                                "mensaje", "Credenciales incorrectas. Verifique su correo electronico y contrasena."));
        }

        @Autowired
        private com.farmacia.sistemaWeb.repository.PasswordResetTokenRepository tokenRepository;

        @Autowired
        private com.farmacia.sistemaWeb.service.EmailService emailService;

        @Autowired
        private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

        @PostMapping("/forgot-password")
        public ResponseEntity<?> forgotPassword(@jakarta.validation.Valid @RequestBody com.farmacia.sistemaWeb.dto.ForgotPasswordDTO dto) {
                Optional<Usuario> optUsuario = usuarioRepository.findByEmail(dto.getEmail());
                
                if (optUsuario.isPresent()) {
                        Usuario usuario = optUsuario.get();
                        // Un solo enlace vigente por usuario: los anteriores dejan de servir. Ademas la
                        // relacion es uno a uno, asi que una segunda solicitud fallaba al guardar.
                        tokenRepository.deleteByUsuario(usuario);
                        String token = java.util.UUID.randomUUID().toString();
                        
                        com.farmacia.sistemaWeb.entity.PasswordResetToken resetToken = new com.farmacia.sistemaWeb.entity.PasswordResetToken();
                        resetToken.setToken(token);
                        resetToken.setUsuario(usuario);
                        resetToken.setFechaExpiracion(java.time.LocalDateTime.now().plusHours(1));
                        tokenRepository.save(resetToken);
                        
                        emailService.enviarEnlaceResetPassword(usuario.getEmail(), usuario.getNombres(), token);
                }
                
                // Siempre devolver success para no revelar si el email existe o no
                return ResponseEntity.ok(Map.of("mensaje", "Si el correo existe en nuestro sistema, recibirá un enlace para restablecer su contraseña."));
        }

        @PostMapping("/reset-password")
        public ResponseEntity<?> resetPassword(@jakarta.validation.Valid @RequestBody com.farmacia.sistemaWeb.dto.ResetPasswordDTO dto) {
                Optional<com.farmacia.sistemaWeb.entity.PasswordResetToken> optToken = tokenRepository.findByToken(dto.getToken());
                
                if (optToken.isEmpty() || optToken.get().isUsado() || optToken.get().isExpired()) {
                        return ResponseEntity.badRequest().body(Map.of("error", "El enlace es inválido o ha expirado."));
                }
                
                com.farmacia.sistemaWeb.entity.PasswordResetToken resetToken = optToken.get();
                Usuario usuario = resetToken.getUsuario();
                
                // Validar fortaleza de la nueva contraseña
                String password = dto.getNewPassword();
                if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
                        return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos una mayúscula, una minúscula y un número."));
                }
                
                usuario.setPassword(passwordEncoder.encode(password));
                usuarioRepository.save(usuario);
                
                resetToken.setUsado(true);
                tokenRepository.save(resetToken);
                
                return ResponseEntity.ok(Map.of("mensaje", "Su contraseña ha sido restablecida exitosamente."));
        }

        // ── REGISTRO PÚBLICO DE CLIENTES ──

        @Autowired
        private com.farmacia.sistemaWeb.service.RegistroClienteService registroClienteService;

        @PostMapping("/registro")
        public ResponseEntity<?> registroCliente(@jakarta.validation.Valid @RequestBody com.farmacia.sistemaWeb.dto.RegistroClienteDTO dto) {
                try {
                        com.farmacia.sistemaWeb.entity.Usuario usuario = registroClienteService.registrar(dto);

                        // Auto-login: generar token para el nuevo cliente
                        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken =
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        dto.getEmail(), dto.getPassword());
                        org.springframework.security.core.Authentication auth = authManager.authenticate(authToken);
                        String token = jwtProvider.generarToken(auth);

                        return ResponseEntity.ok(new com.farmacia.sistemaWeb.dto.LoginResponse(
                                usuario.getId(),
                                usuario.getEmail(),
                                usuario.getNombres(),
                                usuario.getRol().getNombre().name(),
                                token));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
                }
        }
}