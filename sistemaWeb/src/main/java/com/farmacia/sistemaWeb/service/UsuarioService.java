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

    // ── HELPERS INTERNOS ──────────────────────────────────────────────────────

    /**
     * Recupera el usuario autenticado desde el SecurityContext.
     * Compatible con JwtFilter: el principal es el email (String) almacenado
     * en el claim "sub" del token JWT.
     */
    private Usuario usuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
    }

    private boolean esAdmin(Usuario usuario) {
        return usuario.getRol().getNombre() == Rol.NombreRol.ADMIN;
    }

    private void validarAdminOMismoUsuario(Long id) {
        Usuario autenticado = usuarioAutenticado();
        if (!esAdmin(autenticado) && !autenticado.getId().equals(id)) {
            throw new RuntimeException("No tiene permisos para modificar este usuario.");
        }
    }

    // ── REGISTRO ──────────────────────────────────────────────────────────────

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
        Usuario admin = usuarioAutenticado();

        if (!esAdmin(admin)) {
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
        Usuario admin = usuarioAutenticado();

        if (!esAdmin(admin)) {
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
        if (dto.getApellidos() != null) {
            usuario.setApellidos(dto.getApellidos());
        }

        // Asignar el rol enviado desde el frontend; RECEPCIONISTA por defecto
        if (dto.getRol() != null && !dto.getRol().isEmpty()) {
            Rol rol = rolRepository.findByNombre(Rol.NombreRol.valueOf(dto.getRol()))
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + dto.getRol()));
            usuario.setRol(rol);
        } else {
            Rol rolRecepcionista = rolRepository.findByNombre(Rol.NombreRol.RECEPCIONISTA)
                    .orElseThrow(() -> new RuntimeException("Rol RECEPCIONISTA no existe"));
            usuario.setRol(rolRecepcionista);
        }

        usuario = usuarioRepository.save(usuario);

        // Enviar credenciales por email con el rol asignado
        String rolTexto = dto.getRol() != null ? dto.getRol() : "RECEPCIONISTA";
        emailService.enviarCredenciales(dto.getEmail(), dto.getNombres(), dto.getPassword(), rolTexto);

        return usuario;
    }

    // ── MÉTODOS CRUD ──────────────────────────────────────────────────────────

    public java.util.List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario actualizarUsuario(Long id, UsuarioDTO dto) {
        Usuario autenticado = usuarioAutenticado();
        boolean admin = esAdmin(autenticado);

        if (!admin && !autenticado.getId().equals(id)) {
            throw new RuntimeException("No tiene permisos para modificar este usuario.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombres(dto.getNombres());
        if (dto.getApellidos() != null) {
            usuario.setApellidos(dto.getApellidos());
        }
        usuario.setEmail(dto.getEmail());

        if (admin && dto.getRol() != null && !dto.getRol().isEmpty()) {
            Rol rol = rolRepository.findByNombre(Rol.NombreRol.valueOf(dto.getRol()))
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + dto.getRol()));
            usuario.setRol(rol);
        }

        return usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Proteccion de seguridad: Evitar borrar al Administrador Principal del sistema (ID 1)
        if (Long.valueOf(1L).equals(usuario.getId())) {
            throw new RuntimeException("Accion DENEGADA: No puedes eliminar al Administrador Principal del sistema.");
        }

        usuarioRepository.deleteById(id);
    }

    public void cambiarPassword(Long id, String newPassword) {
        Usuario adminOUser = usuarioAutenticado();
        boolean admin = esAdmin(adminOUser);

        if (admin) {
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
        validarAdminOMismoUsuario(id);
        if (imagenBase64 != null && imagenBase64.length() > 5_000_000) {
            throw new RuntimeException("La imagen de perfil supera el tamaño permitido.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setFotoUrl(imagenBase64);
        usuarioRepository.save(usuario);
    }

    public String obtenerImagen(Long id) {
        validarAdminOMismoUsuario(id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuario.getFotoUrl();
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
