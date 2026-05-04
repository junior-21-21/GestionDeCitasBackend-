package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.UsuarioDTO;
import com.farmacia.sistemaWeb.entity.Rol;
import com.farmacia.sistemaWeb.entity.Usuario;
import com.farmacia.sistemaWeb.repository.RolRepository;
import com.farmacia.sistemaWeb.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    // Validar fortaleza de la contrasena
    private void validarPasswordFuerte(String password) {
        if (password == null || password.length() < 6) {
            throw new RuntimeException("La contrasena debe tener al menos 6 caracteres");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("La contrasena debe tener al menos una letra mayuscula");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException("La contrasena debe tener al menos una letra minuscula");
        }
        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException("La contrasena debe tener al menos un numero");
        }
    }

    private void validarPasswordTemporal(String password) {
        if (password == null || password.length() < 6) {
            throw new RuntimeException("La contrasena temporal debe tener al menos 6 caracteres");
        }
    }

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    public Usuario registrarPrimerUsuario(UsuarioDTO dto) {
        if (usuarioRepository.existsByRol_Nombre(Rol.NombreRol.ADMIN)) {
            throw new RuntimeException("El administrador ya ha sido creado");
        }

        validarPasswordFuerte(dto.getPassword());

        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setNombres(dto.getNombres());

        Rol rolAdmin = rolRepository.findByNombre(Rol.NombreRol.ADMIN)
                .orElseThrow(() -> new RuntimeException("Rol ADMIN no existe"));
        usuario.setRol(rolAdmin);

        usuario = usuarioRepository.save(usuario);

        // Enviar credenciales por email
        emailService.enviarCredenciales(dto.getEmail(), dto.getNombres(), dto.getPassword(), "ADMIN");

        return usuario;
    }

    public Usuario registrarVendedor(UsuarioDTO dto) {
        // Obtener el usuario autenticado
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername(); // Spring Security devuelve email como username
        } else {
            email = principal.toString();
        }

        Usuario admin = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = admin.getRol().getNombre() == Rol.NombreRol.ADMIN;

        if (!esAdmin) {
            throw new RuntimeException("Solo el administrador puede registrar vendedores");
        }

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }

        validarPasswordTemporal(dto.getPassword());

        Usuario vendedor = new Usuario();
        vendedor.setEmail(dto.getEmail());
        vendedor.setPassword(passwordEncoder.encode(dto.getPassword()));
        vendedor.setNombres(dto.getNombres());

        Rol rolRecepcionista = rolRepository.findByNombre(Rol.NombreRol.RECEPCIONISTA)
                .orElseThrow(() -> new RuntimeException("Rol RECEPCIONISTA no existe"));
        vendedor.setRol(rolRecepcionista);

        vendedor = usuarioRepository.save(vendedor);

        // Enviar credenciales por email
        emailService.enviarCredenciales(dto.getEmail(), dto.getNombres(), dto.getPassword(), "RECEPCIONISTA");

        return vendedor;
    }

    public Usuario crearUsuarioConRoles(UsuarioDTO dto) {
        // Verificar que el usuario autenticado sea ADMIN
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        Usuario admin = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = admin.getRol().getNombre() == Rol.NombreRol.ADMIN;

        if (!esAdmin) {
            throw new RuntimeException("Solo el administrador puede crear usuarios");
        }

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }

        validarPasswordTemporal(dto.getPassword());

        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setNombres(dto.getNombres());

        // Asignar los roles enviados desde el frontend
        if (dto.getRol() != null && !dto.getRol().isEmpty()) {
            Rol rol = rolRepository.findByNombre(Rol.NombreRol.valueOf(dto.getRol()))
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + dto.getRol()));
            usuario.setRol(rol);
        } else {
            // Si no se envían roles, asignar RECEPCIONISTA por defecto
            Rol rolRecepcionista = rolRepository.findByNombre(Rol.NombreRol.RECEPCIONISTA)
                    .orElseThrow(() -> new RuntimeException("Rol RECEPCIONISTA no existe"));
            usuario.setRol(rolRecepcionista);
        }

        usuario = usuarioRepository.save(usuario);

        // Enviar credenciales por email con el rol correcto
        String rolTexto = dto.getRol() != null ? dto.getRol() : "RECEPCIONISTA";
        emailService.enviarCredenciales(dto.getEmail(), dto.getNombres(), dto.getPassword(), rolTexto);

        return usuario;
    }

    public Usuario loginUsuario(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return usuario;
    }

    // --- MÉTODOS CRUD ---

    public java.util.List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario actualizarUsuario(Long id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombres(dto.getNombres());
        usuario.setEmail(dto.getEmail());

        // Actualizar rol si se envía
        if (dto.getRol() != null && !dto.getRol().isEmpty()) {
            Rol rol = rolRepository.findByNombre(Rol.NombreRol.valueOf(dto.getRol()))
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + dto.getRol()));
            usuario.setRol(rol);
        }

        return usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    public void cambiarPassword(Long id, String newPassword) {
        // Verificar quién está ejecutando la acción
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String emailAutenticado = "";
        if (principal instanceof UserDetails) {
            emailAutenticado = ((UserDetails) principal).getUsername();
        } else {
            emailAutenticado = principal.toString();
        }

        Usuario adminOUser = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = adminOUser.getRol().getNombre() == Rol.NombreRol.ADMIN;

        if (esAdmin) {
            // El Admin cambia cualquier contraseña (incluida la suya): PERMITIR TEMPORAL
            validarPasswordTemporal(newPassword);
        } else {
            // Usuario NORMAL cambia su propia contraseña: EXIGIR FUERTE
            if (!adminOUser.getId().equals(id)) {
                throw new RuntimeException("No tiene permisos para modificar este usuario.");
            }
            validarPasswordFuerte(newPassword);
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }

    public void actualizarImagen(Long id, String imagenBase64) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setImagenPerfil(imagenBase64);
        usuarioRepository.save(usuario);
    }

    public String obtenerImagen(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuario.getImagenPerfil();
    }

    public void desbloquearCuenta(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setCuentaBloqueada(false);
        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);
    }

    public void cambiarEstadoCuenta(Long id, boolean estado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setHabilitada(estado);
        usuarioRepository.save(usuario);
    }
}
