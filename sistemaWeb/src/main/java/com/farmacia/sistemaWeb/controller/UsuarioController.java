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
}
