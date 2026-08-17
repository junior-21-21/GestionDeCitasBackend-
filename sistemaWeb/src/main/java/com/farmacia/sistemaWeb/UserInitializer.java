package com.farmacia.sistemaWeb;

import com.farmacia.sistemaWeb.entity.*;
import com.farmacia.sistemaWeb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Component
public class UserInitializer implements CommandLineRunner {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private EspecialidadRepository especialidadRepository;
    @Autowired private VeterinarioRepository veterinarioRepository;
    @Autowired private EspecieRepository especieRepository;
    @Autowired private RazaRepository razaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private CitaRepository citaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Asegurar roles
        Rol rolAdmin = asegurarRol(Rol.NombreRol.ADMIN);
        Rol rolRecepcionista = asegurarRol(Rol.NombreRol.RECEPCIONISTA);
        Rol rolVeterinario = asegurarRol(Rol.NombreRol.VETERINARIO);
        Rol rolCliente = asegurarRol(Rol.NombreRol.CLIENTE);

        // 2. Asegurar especialidad general
        Especialidad espGeneral = asegurarEspecialidad("General");

        // 3. Crear Usuarios Base
        crearUsuario("quicanomorenojunior21072004@gmail.com", "admin123", "Junior Quicano (Admin)", rolAdmin);
        crearUsuario("recepcion.petyzoos@gmail.com", "recep123", "Recepcionista PetyZoos", rolRecepcionista);
        Usuario vetUsuario = crearUsuario("veterinario.petyzoos@gmail.com", "vet123", "Veterinario PetyZoos", rolVeterinario);
        
        // 4. Crear Perfil Médico (Veterinario)
        Veterinario veterinario = null;
        if (vetUsuario != null) {
            veterinario = asegurarVeterinario("12345678", "Veterinario", "PetyZoos", "veterinario.petyzoos@gmail.com", espGeneral, vetUsuario);
        }

        // 5. Crear Datos Clínicos de Prueba (Especies, Razas)
        Especie especiePerro = asegurarEspecie("Perro");
        Especie especieGato = asegurarEspecie("Gato");
        Raza razaLabrador = asegurarRaza("Labrador", especiePerro);
        Raza razaPersa = asegurarRaza("Persa", especieGato);

        // 6. Crear Clientes de Prueba
        Cliente cliente1 = asegurarCliente("DNI001", "Carlos", "Perez", "999888777", "carlos@gmail.com", "Av. Los Pinos 123");
        Cliente cliente2 = asegurarCliente("DNI002", "Ana", "Gomez", "999666555", "ana@gmail.com", "Calle Las Flores 456");

        // 7. Crear Pacientes de Prueba
        Paciente paciente1 = asegurarPaciente("PAC-001", "Max", razaLabrador, cliente1);
        Paciente paciente2 = asegurarPaciente("PAC-002", "Luna", razaPersa, cliente2);

        // 8. Crear Citas de Prueba (Para hoy)
        if (veterinario != null) {
            asegurarCita("CITA-001", LocalDate.now(), LocalTime.of(10, 0), "Consulta General", paciente1, veterinario, Cita.EstadoCita.PENDIENTE);
            asegurarCita("CITA-002", LocalDate.now(), LocalTime.of(11, 30), "Vacunación", paciente2, veterinario, Cita.EstadoCita.PENDIENTE);
            asegurarCita("CITA-003", LocalDate.now().minusDays(1), LocalTime.of(15, 0), "Control post-operatorio", paciente1, veterinario, Cita.EstadoCita.REALIZADA);
        }
    }

    private Rol asegurarRol(Rol.NombreRol nombre) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre(nombre);
            return rolRepository.save(r);
        });
    }
    
    private Especialidad asegurarEspecialidad(String nombre) {
        if (especialidadRepository.count() == 0) {
            Especialidad e = new Especialidad();
            e.setNombre(nombre);
            return especialidadRepository.save(e);
        }
        return especialidadRepository.findAll().get(0);
    }

    private Usuario crearUsuario(String email, String pwd, String nombres, Rol rol) {
        return usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(pwd));
            u.setNombres(nombres);
            u.setRol(rol);
            u.setHabilitada(true);
            u.setCuentaBloqueada(false);
            u.setIntentosFallidos(0);
            return usuarioRepository.save(u);
        });
    }
    
    private Veterinario asegurarVeterinario(String dni, String nombres, String apellidos, String correo, Especialidad esp, Usuario usuario) {
        return veterinarioRepository.findByCorreo(correo).orElseGet(() -> {
            Veterinario v = new Veterinario();
            v.setDni(dni);
            v.setNombres(nombres);
            v.setApellidos(apellidos);
            v.setCorreo(correo);
            v.setEspecialidad(esp);
            v.setUsuario(usuario);
            return veterinarioRepository.save(v);
        });
    }

    private Especie asegurarEspecie(String nombre) {
        if (especieRepository.count() == 0 || especieRepository.findAll().stream().noneMatch(e -> e.getNombre().equals(nombre))) {
            Especie e = new Especie();
            e.setNombre(nombre);
            return especieRepository.save(e);
        }
        return especieRepository.findAll().stream().filter(e -> e.getNombre().equals(nombre)).findFirst().get();
    }

    private Raza asegurarRaza(String nombre, Especie especie) {
        if (razaRepository.count() == 0 || razaRepository.findAll().stream().noneMatch(r -> r.getNombre().equals(nombre))) {
            Raza r = new Raza();
            r.setNombre(nombre);
            r.setEspecie(especie);
            return razaRepository.save(r);
        }
        return razaRepository.findAll().stream().filter(r -> r.getNombre().equals(nombre)).findFirst().get();
    }

    private Cliente asegurarCliente(String documentoId, String nombres, String apellidos, String telefono, String correo, String direccion) {
        return clienteRepository.findById(documentoId).orElseGet(() -> {
            Cliente c = new Cliente();
            c.setDni(documentoId);
            c.setNombres(nombres);
            c.setApellidos(apellidos);
            c.setEmail(correo);
            c.setCalle(direccion);
            return clienteRepository.save(c);
        });
    }

    private Paciente asegurarPaciente(String codigo, String nombre, Raza raza, Cliente cliente) {
        return pacienteRepository.findById(codigo).orElseGet(() -> {
            Paciente p = new Paciente();
            p.setCodigoPaciente(codigo);
            p.setNombre(nombre);
            p.setRaza(raza);
            p.setCliente(cliente);
            p.setFechaNacimiento(LocalDate.now().minusYears(2));
            return pacienteRepository.save(p);
        });
    }

    private void asegurarCita(String codigo, LocalDate fecha, LocalTime hora, String motivo, Paciente paciente, Veterinario veterinario, Cita.EstadoCita estado) {
        if (!citaRepository.existsById(codigo)) {
            Cita c = new Cita();
            c.setCodigoCita(codigo);
            c.setFecha(fecha);
            c.setHora(hora);
            c.setMotivo(motivo);
            c.setPaciente(paciente);
            c.setVeterinario(veterinario);
            c.setEstado(estado);
            c.setDuracionMinutos(30);
            citaRepository.save(c);
        }
    }
}
