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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );

            Usuario usuario = usuarioRepository.findByUsername(dto.getUsername()).orElseThrow();
            String token = jwtProvider.generarToken(auth);

            return ResponseEntity.ok(new LoginResponse(
                    usuario.getId(), // 👈 AGREGADO
                    usuario.getUsername(),
                    usuario.getNombres(),
                    usuario.getRoles().stream()
                            .map(r -> Map.of("nombre", r.getNombre().name()))
                            .toList(),
                    token
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
    }
}