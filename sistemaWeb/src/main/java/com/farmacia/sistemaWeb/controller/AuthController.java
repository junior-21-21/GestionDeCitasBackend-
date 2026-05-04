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

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private static final int MAX_INTENTOS = 5;

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

                // Verificar si el usuario existe
                Optional<Usuario> optUsuario = usuarioRepository.findByEmail(dto.getEmail());
                if (optUsuario.isEmpty()) {
                        // No revelamos si el email existe o no (seguridad)
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                        Map.of(
                                                        "error", "CREDENCIALES_INCORRECTAS",
                                                        "mensaje", "Verifique su correo electronico y contrasena.",
                                                        "intentosRestantes", MAX_INTENTOS));
                }

                Usuario usuario = optUsuario.get();

                // Verificar si la cuenta esta inhabilitada manualmente
                if (!usuario.isHabilitada()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                        Map.of(
                                                        "error", "CUENTA_INHABILITADA",
                                                        "mensaje",
                                                        "Su cuenta ha sido inhabilitada temporalmente por el administrador. Contactelo para más informacion."));
                }

                // Verificar si la cuenta esta bloqueada por intentos fallidos
                if (usuario.isCuentaBloqueada()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                        Map.of(
                                                        "error", "CUENTA_BLOQUEADA",
                                                        "mensaje",
                                                        "Su cuenta ha sido bloqueada por multiples intentos fallidos. Contacte al administrador para desbloquearla.",
                                                        "intentos", MAX_INTENTOS));
                }

                try {
                        // Intentar autenticacion
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

                } catch (Exception e) {
                        // Login fallido: incrementar intentos
                        int intentos = usuario.getIntentosFallidos() + 1;
                        usuario.setIntentosFallidos(intentos);

                        if (intentos >= MAX_INTENTOS) {
                                // Bloquear la cuenta
                                usuario.setCuentaBloqueada(true);
                                usuarioRepository.save(usuario);
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                                Map.of(
                                                                "error", "CUENTA_BLOQUEADA",
                                                                "mensaje",
                                                                "Su cuenta ha sido bloqueada por " + MAX_INTENTOS
                                                                                + " intentos fallidos. Contacte al administrador para desbloquearla.",
                                                                "intentos", MAX_INTENTOS));
                        }

                        usuarioRepository.save(usuario);
                        int restantes = MAX_INTENTOS - intentos;
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                        Map.of(
                                                        "error", "CREDENCIALES_INCORRECTAS",
                                                        "mensaje",
                                                        "Credenciales incorrectas. Le quedan " + restantes
                                                                        + " intento(s) antes de que su cuenta sea bloqueada.",
                                                        "intentosRestantes", restantes));
                }
        }
}