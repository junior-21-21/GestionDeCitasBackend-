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

        // 2. Asegurar especialidades
        Especialidad espGeneral = asegurarEspecialidad("General");
        Especialidad espOdontologia = asegurarEspecialidad("Odontología");
        Especialidad espCirugia = asegurarEspecialidad("Cirugía");

        // 3. Crear Usuarios Base
        crearUsuario("quicanomorenojunior21072004@gmail.com", "admin123", "Junior Quicano (Admin)", rolAdmin);
        crearUsuario("recepcion.petyzoos@gmail.com", "recep123", "Recepcionista PetyZoos", rolRecepcionista);
        
        // 4. Crear Perfiles Médicos (Veterinarios)
        Usuario vet1 = crearUsuario("juancarlosvet@gmail.com", "vet12345", "JUANCARLOS FLORES CHUNGA", rolVeterinario);
        Veterinario vGeneral = asegurarVeterinario("87654321", "JUANCARLOS", "FLORES CHUNGA", "juancarlosvet@gmail.com", espGeneral, vet1);

        Usuario vet2 = crearUsuario("dr.perez.vet@gmail.com", "vet12345", "Carlos Pérez", rolVeterinario);
        Veterinario vOdontologo = asegurarVeterinario("22334455", "Carlos", "Pérez", "dr.perez.vet@gmail.com", espOdontologia, vet2);

        Usuario vet3 = crearUsuario("dra.garcia.vet@gmail.com", "vet12345", "Ana García", rolVeterinario);
        Veterinario vCirujano = asegurarVeterinario("66778899", "Ana", "García", "dra.garcia.vet@gmail.com", espCirugia, vet3);

        // 5. Crear Datos Clínicos de Prueba (Especies, Razas)
        Especie especiePerro = asegurarEspecie("Perro");
        Especie especieGato = asegurarEspecie("Gato");
        Raza razaLabrador = asegurarRaza("Labrador", especiePerro);
        Raza razaPersa = asegurarRaza("Persa", especieGato);

        // 6. Crear Clientes de Prueba (más masivo)
        String[] nombresCli = {"Carlos", "Ana", "Luis", "Maria", "Jorge", "Lucia", "Pedro", "Sofia", "Miguel", "Elena"};
        String[] apellidosCli = {"Perez", "Gomez", "Lopez", "Diaz", "Torres", "Ruiz", "Vargas", "Castro", "Ramos", "Flores"};
        java.util.List<Cliente> clientes = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            clientes.add(asegurarCliente("DNI00" + i, nombresCli[i], apellidosCli[i], "99988877" + i, nombresCli[i].toLowerCase() + "@gmail.com", "Calle " + i));
        }

        // 7. Crear Pacientes de Prueba (más masivo)
        String[] nombresPac = {"Max", "Luna", "Rocky", "Bella", "Toby", "Kira", "Coco", "Mia", "Leo", "Nala", "Zeus", "Lola", "Thor", "Chloe", "Simba"};
        java.util.List<Paciente> pacientesList = new java.util.ArrayList<>();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 15; i++) {
            Raza r = random.nextBoolean() ? razaLabrador : razaPersa;
            Cliente c = clientes.get(random.nextInt(clientes.size()));
            pacientesList.add(asegurarPaciente("PAC-00" + i, nombresPac[i], r, c));
        }

        // 8. Crear Citas de Prueba para UN MES COMPLETO (pasado, presente y futuro)
        int citaId = 100;
        // Empezamos 20 días atrás y terminamos 10 días en el futuro (30 días en total)
        LocalDate fechaInicio = LocalDate.now().minusDays(20); 
        
        String[] motivos = {"Consulta General", "Vacunación", "Desparasitación", "Revisión", "Control post-operatorio", "Cirugía menor", "Chequeo Dental", "Limpieza Dental"};
        
        for (int i = 0; i < 30; i++) {
            LocalDate fechaCita = fechaInicio.plusDays(i);
            // No agendar domingos
            if (fechaCita.getDayOfWeek().getValue() == 7) continue;
            
            Cita.EstadoCita estado = fechaCita.isBefore(LocalDate.now()) ? Cita.EstadoCita.REALIZADA : Cita.EstadoCita.PENDIENTE;
            
            // Generar entre 2 y 6 citas por día
            int numCitasDia = random.nextInt(5) + 2; 
            
            for (int j = 0; j < numCitasDia; j++) {
                int hora = 9 + random.nextInt(9); // 9 a 17 horas
                int minuto = random.nextBoolean() ? 0 : 30; // Minuto 00 o 30
                
                String motivo = motivos[random.nextInt(motivos.length)];
                Paciente pac = pacientesList.get(random.nextInt(pacientesList.size()));
                
                Veterinario vetAsignado = vGeneral;
                if (motivo.contains("Dental")) {
                    vetAsignado = vOdontologo;
                } else if (motivo.contains("Cirugía") || motivo.contains("post-operatorio")) {
                    vetAsignado = vCirujano;
                }
                
                asegurarCita(String.format("CITA-TEST-%03d", citaId++), fechaCita, LocalTime.of(hora, minuto), motivo, pac, vetAsignado, estado);
            }
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
        if (especialidadRepository.count() == 0 || especialidadRepository.findAll().stream().noneMatch(e -> e.getNombre().equals(nombre))) {
            Especialidad e = new Especialidad();
            e.setNombre(nombre);
            return especialidadRepository.save(e);
        }
        return especialidadRepository.findAll().stream().filter(e -> e.getNombre().equals(nombre)).findFirst().get();
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
