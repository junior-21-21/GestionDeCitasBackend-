package com.farmacia.sistemaWeb.util;

import com.farmacia.sistemaWeb.entity.*;
import com.farmacia.sistemaWeb.repository.*;

import com.farmacia.sistemaWeb.service.EspecieRazaService;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.Arrays;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EspecialidadRepository especialidadRepository;
    @Autowired
    private VeterinarioRepository veterinarioRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private ConsultaRepository consultaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EspecieRazaService especieRazaService;



    @Override
    public void run(String... args) throws Exception {
        // === Roles (crear individualmente si no existen) ===
        if (rolRepository.findByNombre(Rol.NombreRol.ADMIN).isEmpty()) {
            Rol admin = new Rol();
            admin.setNombre(Rol.NombreRol.ADMIN);
            rolRepository.save(admin);
        }
        if (rolRepository.findByNombre(Rol.NombreRol.RECEPCIONISTA).isEmpty()) {
            Rol recep = new Rol();
            recep.setNombre(Rol.NombreRol.RECEPCIONISTA);
            rolRepository.save(recep);
        }
        if (rolRepository.findByNombre(Rol.NombreRol.VETERINARIO).isEmpty()) {
            Rol vet = new Rol();
            vet.setNombre(Rol.NombreRol.VETERINARIO);
            rolRepository.save(vet);
        }
        System.out.println("✅ Roles verificados/creados");

        // === Seed de Especies y Razas (Normalización A) ===
        seedEspeciesYRazas();

        // === Migración: Asignar raza a pacientes existentes sin raza ===
        migrarPacientesSinRaza();

        // === Admin User ===
        if (usuarioRepository.count() == 0) {
            Rol adminRol = rolRepository.findByNombre(Rol.NombreRol.ADMIN).orElseThrow();
            Usuario adminUser = new Usuario();
            adminUser.setEmail("quicanomorenojunior21072004@gmail.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setNombres("Admin Sistema");
            adminUser.setRol(adminRol);
            usuarioRepository.save(adminUser);
            System.out.println("✅ Usuario admin creado");
        }

        // === Especialidades ===
        String[] especialidades = { "Cirugía", "Dermatología", "Cardiología", "Traumatología", "Medicina General" };
        for (String e : especialidades) {
            if (especialidadRepository.findByNombre(e).isEmpty()) {
                Especialidad esp = new Especialidad();
                esp.setNombre(e);
                especialidadRepository.save(esp);
            }
        }
        System.out.println("✅ Especialidades creadas/verificadas");

        // === Veterinarios ===
        Especialidad cirugia = especialidadRepository.findByNombre("Cirugía").orElseThrow();
        Especialidad cardio = especialidadRepository.findByNombre("Cardiología").orElseThrow();
        Rol vetRol = rolRepository.findByNombre(Rol.NombreRol.VETERINARIO).orElseThrow();

        // Vet 1
        if (usuarioRepository.findByEmail("dr.perez.vet@gmail.com").isEmpty()) {
            Usuario u1 = new Usuario();
            u1.setEmail("dr.perez.vet@gmail.com");
            u1.setPassword(passwordEncoder.encode("vet123"));
            u1.setNombres("Carlos Pérez"); // Usuario guarda nombre completo
            u1.setRol(vetRol);
            usuarioRepository.save(u1);

            Veterinario v1 = new Veterinario();
            v1.setDni("12345678");
            v1.setNombres("Carlos");
            v1.setApellidos("Pérez");
            v1.setCelular("987654321");
            v1.setCorreo("dr.perez.vet@gmail.com");
            v1.setEspecialidad(cirugia);
            v1.setUsuario(u1);
            veterinarioRepository.save(v1);
        }

        // Vet 2
        if (usuarioRepository.findByEmail("dra.garcia.vet@gmail.com").isEmpty()) {
            Usuario u2 = new Usuario();
            u2.setEmail("dra.garcia.vet@gmail.com");
            u2.setPassword(passwordEncoder.encode("vet123"));
            u2.setNombres("Ana García");
            u2.setRol(vetRol);
            usuarioRepository.save(u2);

            Veterinario v2 = new Veterinario();
            v2.setDni("87654321");
            v2.setNombres("Ana");
            v2.setApellidos("García");
            v2.setCelular("912345678");
            v2.setCorreo("dra.garcia.vet@gmail.com");
            v2.setEspecialidad(cardio);
            v2.setUsuario(u2);
            veterinarioRepository.save(v2);
        }
        System.out.println("✅ Veterinarios adicionales creados/verificados");

        // === Clientes ===
        if (clienteRepository.count() == 0) {
            Cliente c1 = new Cliente();
            c1.setDni("44556677");
            c1.setNombres("Juan");
            c1.setApellidos("López");
            c1.setTelefono("999111222"); // maintained for compat
            c1.setCalle("Av. Los Olivos");
            c1.setNumero("123");
            c1.setDistrito("Los Olivos");
            c1.setProvincia("Lima");
            clienteRepository.save(c1);

            Cliente c2 = new Cliente();
            c2.setDni("11223344");
            c2.setNombres("María");
            c2.setApellidos("Torres");
            c2.setTelefono("999333444");
            c2.setCalle("Jr. Primavera");
            c2.setNumero("456");
            c2.setDistrito("San Borja");
            c2.setProvincia("Lima");
            clienteRepository.save(c2);

            System.out.println("✅ Clientes creados");
        }

        // === Pacientes (Usando Raza normalizada) ===
        if (pacienteRepository.count() == 0) {
            Cliente c1 = clienteRepository.findById("44556677").orElseThrow();
            Cliente c2 = clienteRepository.findById("11223344").orElseThrow();

            Raza labrador = especieRazaService.obtenerOCrearRaza("Perro", "Labrador");
            Raza siames = especieRazaService.obtenerOCrearRaza("Gato", "Siamés");
            Raza pastorAleman = especieRazaService.obtenerOCrearRaza("Perro", "Pastor Alemán");

            Paciente p1 = new Paciente();
            p1.setCodigoPaciente("PAC-FIRO-001");
            p1.setNombre("Firulais");
            p1.setRaza(labrador);
            p1.setFechaNacimiento(LocalDate.now().minusYears(3));
            p1.setCliente(c1);
            pacienteRepository.save(p1);

            Paciente p2 = new Paciente();
            p2.setCodigoPaciente("PAC-MICH-001");
            p2.setNombre("Michi");
            p2.setRaza(siames);
            p2.setFechaNacimiento(LocalDate.now().minusYears(2));
            p2.setCliente(c2);
            pacienteRepository.save(p2);

            Paciente p3 = new Paciente();
            p3.setCodigoPaciente("PAC-TOBY-001");
            p3.setNombre("Toby");
            p3.setRaza(pastorAleman);
            p3.setFechaNacimiento(LocalDate.now().minusYears(5));
            p3.setCliente(c1);
            pacienteRepository.save(p3);

            System.out.println("✅ Pacientes creados (normalizado)");
        }

        // === Citas ===
        if (citaRepository.count() == 0) {
            Paciente p1 = pacienteRepository.findById("PAC-FIRO-001").orElseThrow();
            Paciente p2 = pacienteRepository.findById("PAC-MICH-001").orElseThrow();
            Veterinario v1 = veterinarioRepository.findById("12345678").orElseThrow();
            Veterinario v2 = veterinarioRepository.findById("87654321").orElseThrow();

            Cita cita1 = new Cita();
            cita1.setCodigoCita("CIT-20260305-001");
            cita1.setFecha(LocalDate.of(2026, 3, 5));
            cita1.setHora(LocalTime.of(10, 0));
            cita1.setMotivo("Vacunación anual");
            cita1.setDuracionMinutos(30);
            cita1.setEstado(Cita.EstadoCita.PENDIENTE);
            cita1.setPaciente(p1);
            cita1.setVeterinario(v1);
            citaRepository.save(cita1);

            Cita cita2 = new Cita();
            cita2.setCodigoCita("CIT-20260305-002");
            cita2.setFecha(LocalDate.of(2026, 3, 5));
            cita2.setHora(LocalTime.of(11, 0));
            cita2.setMotivo("Control general");
            cita2.setDuracionMinutos(45);
            cita2.setEstado(Cita.EstadoCita.PENDIENTE);
            cita2.setPaciente(p2);
            cita2.setVeterinario(v2);
            citaRepository.save(cita2);

            System.out.println("✅ Citas creadas");
        }

        // === Consultas (Mock Data para HOY) ===
        if (consultaRepository.count() == 0) {
            // Se necesitan citas para asociar consultas
            Cita cita1 = citaRepository.findById("CIT-20260305-001").orElse(null);
            Cita cita2 = citaRepository.findById("CIT-20260305-002").orElse(null);
            
            if (cita1 != null && cita2 != null) {
                Consulta c1 = new Consulta();
                c1.setCodigoConsulta("CNS-HOY-001");
                c1.setFecha(LocalDate.now());
                c1.setMotivo("Vacunación Sextuple");
                c1.setPeso(12.5);
                c1.setObservaciones("Paciente alerta, mucosas rosadas.");
                c1.setDiagnostico("Sano. Vacunación preventiva.");
                c1.setTratamiento("Se aplica vacuna Sextuple SC. Próximo control en 1 año.");
                c1.setCita(cita1);
                consultaRepository.save(c1);
    
                Consulta c2 = new Consulta();
                c2.setCodigoConsulta("CNS-HOY-002");
                c2.setFecha(LocalDate.now());
                c2.setMotivo("Problema de piel, rascado constante");
                c2.setPeso(4.2);
                c2.setObservaciones("Alopecia en zona lumbar. Presencia de pulgas.");
                c2.setDiagnostico("Dermatitis Alérgica a la Picadura de Pulga (DAPP)");
                c2.setTratamiento("Bravecto 1 tab. Baño medicado con Clorhexidina cada 7 días.");
                c2.setCita(cita2);
                consultaRepository.save(c2);
                
                System.out.println("✅ Consultas (Mock HOY) creadas a partir de Citas");
            }
        }

        // === Población masiva de 20 mascotas y citas (NUEVO) ===
        poblarDatosPruebaAdicionales();



        System.out.println("🏁 DataLoader finalizado exitosamente");
    }

    /**
     * Migración: asigna raza a pacientes existentes que fueron creados antes de la normalización.
     * Es idempotente: solo procesa pacientes con raza_id NULL.
     */
    private void migrarPacientesSinRaza() {
        Raza razaDefault = especieRazaService.obtenerOCrearRaza("Perro", "Mestizo");
        java.util.List<Paciente> sinRaza = pacienteRepository.findAll().stream()
                .filter(p -> p.getRaza() == null)
                .toList();

        if (!sinRaza.isEmpty()) {
            for (Paciente p : sinRaza) {
                p.setRaza(razaDefault);
                pacienteRepository.save(p);
            }
            System.out.println("🔄 Migración: " + sinRaza.size() + " pacientes actualizados con raza por defecto (Perro/Mestizo)");
        }
    }

    /**
     * Seed de especies y razas comunes. Se usa obtenerOCrearRaza para evitar duplicados.
     */
    private void seedEspeciesYRazas() {
        // Perro
        String[] razasPerro = {"Labrador", "Pastor Alemán", "Bulldog", "Poodle", "Golden Retriever",
                "Rottweiler", "Beagle", "Husky Siberiano", "Chihuahua", "Pug", "Boxer", "Dálmata",
                "Schnauzer", "Yorkshire Terrier", "Mestizo"};
        for (String r : razasPerro) {
            especieRazaService.obtenerOCrearRaza("Perro", r);
        }

        // Gato
        String[] razasGato = {"Siamés", "Persa", "Angora", "Siberiano", "Bengal", "Maine Coon",
                "Ragdoll", "British Shorthair", "Sphynx", "Mestizo"};
        for (String r : razasGato) {
            especieRazaService.obtenerOCrearRaza("Gato", r);
        }

        // Otras especies
        especieRazaService.obtenerOCrearRaza("Ave", "Periquito");
        especieRazaService.obtenerOCrearRaza("Ave", "Canario");
        especieRazaService.obtenerOCrearRaza("Ave", "Loro");
        especieRazaService.obtenerOCrearRaza("Ave", "Cacatúa");
        especieRazaService.obtenerOCrearRaza("Conejo", "Cabeza de León");
        especieRazaService.obtenerOCrearRaza("Conejo", "Holland Lop");
        especieRazaService.obtenerOCrearRaza("Conejo", "Rex");
        especieRazaService.obtenerOCrearRaza("Hamster", "Sirio");
        especieRazaService.obtenerOCrearRaza("Hamster", "Ruso");
        especieRazaService.obtenerOCrearRaza("Tortuga", "Terrestre");
        especieRazaService.obtenerOCrearRaza("Tortuga", "Acuática");
        especieRazaService.obtenerOCrearRaza("Pez", "Goldfish");
        especieRazaService.obtenerOCrearRaza("Pez", "Betta");
        especieRazaService.obtenerOCrearRaza("Reptil", "Iguana");
        especieRazaService.obtenerOCrearRaza("Reptil", "Gecko");

        System.out.println("✅ Especies y razas verificadas/creadas (Normalización A)");
    }

    private void poblarDatosPruebaAdicionales() {
        if (clienteRepository.count() > 2) {
            return;
        }

        System.out.println("🚀 Iniciando población de 20 mascotas y citas adicionales...");

        String[] nombresDuenos = {
                "Laura", "Sofía", "Diego", "Mateo", "Valentina",
                "Andrés", "Camila", "Sebastián", "Isabella", "Nicolás",
                "Gabriela", "Felipe", "Lucía", "Samuel", "Martina",
                "Daniel", "Elena", "Joaquín", "Victoria", "Esteban"
        };
        String[] apellidosDuenos = {
                "Rojas", "Castro", "Mendoza", "Ortega", "Vargas",
                "Silva", "Pinto", "Ríos", "Morales", "Delgado",
                "Guerra", "Campos", "Navarro", "Acosta", "Reyes",
                "Vega", "Cortés", "Zamora", "Soto", "Ibáñez"
        };
        String[] nombresMascotas = {
                "Rocky", "Luna", "Max", "Bella", "Coco",
                "Molly", "Simba", "Lola", "Toby", "Sasha",
                "Bruno", "Chloe", "Lucky", "Daisy", "Zeus",
                "Nala", "Bento", "Mia", "Thor", "Maya"
        };
        // Ahora usamos la relación normalizada Especie→Raza
        String[][] especieRazaPares = {
                {"Perro", "Pug"}, {"Gato", "Persa"}, {"Perro", "Beagle"}, {"Gato", "Siamés"},
                {"Perro", "Golden Retriever"}, {"Perro", "Boxer"}, {"Conejo", "Cabeza de León"},
                {"Gato", "Angora"}, {"Perro", "Dálmata"}, {"Gato", "Siberiano"}
        };

        Veterinario v1 = veterinarioRepository.findById("12345678").orElse(null);
        Veterinario v2 = veterinarioRepository.findById("87654321").orElse(null);

        if (v1 == null || v2 == null)
            return;

        for (int i = 0; i < 20; i++) {
            // 1. Crear Dueño
            String dni = "202610" + String.format("%02d", i);
            Cliente cl = new Cliente();
            cl.setDni(dni);
            cl.setNombres(nombresDuenos[i]);
            cl.setApellidos(apellidosDuenos[i]);
            cl.setTelefono("9" + (10000000 + i));
            cl.setCalle("Calle de Pruebas");
            cl.setNumero(String.valueOf(100 + i));
            cl.setDistrito("Lima");
            cl.setProvincia("Lima");
            clienteRepository.save(cl);

            // 2. Crear Mascota con Raza normalizada
            String[] par = especieRazaPares[i % especieRazaPares.length];
            Raza raza = especieRazaService.obtenerOCrearRaza(par[0], par[1]);

            String codPaciente = "PAC-" + nombresMascotas[i].toUpperCase() + "-002";
            Paciente p = new Paciente();
            p.setCodigoPaciente(codPaciente);
            p.setNombre(nombresMascotas[i]);
            p.setRaza(raza);
            p.setFechaNacimiento(LocalDate.now().minusYears(1 + (i % 8)));
            p.setCliente(cl);
            pacienteRepository.save(p);

            // 3. Crear Cita
            int dia = 9 + (i % 7);
            LocalDate fechaCita = LocalDate.of(2026, 3, dia);
            LocalTime horaCita = LocalTime.of(8 + (i % 10), (i % 2 == 0 ? 0 : 30));

            Cita cita = new Cita();
            cita.setCodigoCita("CIT-MAS-" + String.format("%03d", i));
            cita.setFecha(fechaCita);
            cita.setHora(horaCita);
            cita.setMotivo("Consulta de prueba " + (i + 1));
            cita.setDuracionMinutos(30);
            cita.setEstado(Cita.EstadoCita.PENDIENTE);
            cita.setPaciente(p);
            cita.setVeterinario(i % 2 == 0 ? v1 : v2);
            citaRepository.save(cita);
        }

        System.out.println("✅ 20 dueños, mascotas y citas creadas exitosamente (normalizado)");
    }


}
