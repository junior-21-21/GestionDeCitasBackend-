package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.RegistroClienteDTO;
import com.farmacia.sistemaWeb.entity.Rol;
import com.farmacia.sistemaWeb.entity.Usuario;
import com.farmacia.sistemaWeb.repository.RolRepository;
import com.farmacia.sistemaWeb.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroClienteService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroClienteService(UsuarioRepository usuarioRepository,
                                   RolRepository rolRepository,
                                   PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrar(RegistroClienteDTO dto) {
        // Validar email único
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo electrónico.");
        }

        // Validar fortaleza de contraseña
        String pwd = dto.getPassword();
        if (!pwd.matches(".*[A-Z].*") || !pwd.matches(".*[a-z].*") || !pwd.matches(".*\\d.*")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos una mayúscula, una minúscula y un número.");
        }

        // Obtener o crear rol CLIENTE
        Rol rolCliente = rolRepository.findByNombre(Rol.NombreRol.CLIENTE)
                .orElseThrow(() -> new RuntimeException("Rol CLIENTE no encontrado en la base de datos."));

        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setRol(rolCliente);
        usuario.setHabilitada(true);
        usuario.setCuentaBloqueada(false);
        usuario.setIntentosFallidos(0);

        return usuarioRepository.save(usuario);
    }
}
