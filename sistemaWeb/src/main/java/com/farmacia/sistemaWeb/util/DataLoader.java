package com.farmacia.sistemaWeb.util;

import com.farmacia.sistemaWeb.entity.*;
import com.farmacia.sistemaWeb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaProductoRepository categoriaProductoRepository;
    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private ConsultaRepository consultaRepository;
    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private MovimientoInventarioRepository movimientoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
        if (especialidadRepository.count() == 0) {
            String[] especialidades = { "Cirugía", "Dermatología", "Cardiología", "Traumatología", "Medicina General" };
            for (String e : especialidades) {
                Especialidad esp = new Especialidad();
                esp.setNombre(e);
                especialidadRepository.save(esp);
            }
            System.out.println("✅ Especialidades creadas");
        }

        // === Veterinarios ===
        if (veterinarioRepository.count() == 0) {
            Especialidad cirugia = especialidadRepository.findAll().get(0);
            Especialidad cardio = especialidadRepository.findAll().get(2);
            Rol vetRol = rolRepository.findByNombre(Rol.NombreRol.VETERINARIO).orElseThrow();

            // Vet 1
            Usuario u1 = new Usuario();
            u1.setEmail("dr.perez@petyzoos.com");
            u1.setPassword(passwordEncoder.encode("vet123"));
            u1.setNombres("Carlos Pérez");
            u1.setRol(vetRol);

            Veterinario v1 = new Veterinario();
            v1.setDni("12345678");
            v1.setNombres("Carlos Pérez");
            v1.setCelular("987654321");
            v1.setCorreo("dr.perez@petyzoos.com");
            v1.setEspecialidad(cirugia);
            v1.setUsuario(u1);
            veterinarioRepository.save(v1);

            // Vet 2
            Usuario u2 = new Usuario();
            u2.setEmail("dra.garcia@petyzoos.com");
            u2.setPassword(passwordEncoder.encode("vet123"));
            u2.setNombres("Ana García");
            u2.setRol(vetRol);

            Veterinario v2 = new Veterinario();
            v2.setDni("87654321");
            v2.setNombres("Ana García");
            v2.setCelular("912345678");
            v2.setCorreo("dra.garcia@petyzoos.com");
            v2.setEspecialidad(cardio);
            v2.setUsuario(u2);
            veterinarioRepository.save(v2);

            System.out.println("✅ Veterinarios creados");
        }

        // === Clientes ===
        if (clienteRepository.count() == 0) {
            Cliente c1 = new Cliente();
            c1.setDni("44556677");
            c1.setNombres("Juan");
            c1.setApellidos("López");
            c1.setTelefono("999111222");
            c1.setDireccion("Av. Los Olivos 123");
            clienteRepository.save(c1);

            Cliente c2 = new Cliente();
            c2.setDni("11223344");
            c2.setNombres("María");
            c2.setApellidos("Torres");
            c2.setTelefono("999333444");
            c2.setDireccion("Jr. Primavera 456");
            clienteRepository.save(c2);

            System.out.println("✅ Clientes creados");
        }

        // === Pacientes ===
        if (pacienteRepository.count() == 0) {
            Cliente c1 = clienteRepository.findById("44556677").orElseThrow();
            Cliente c2 = clienteRepository.findById("11223344").orElseThrow();

            Paciente p1 = new Paciente();
            p1.setCodigoPaciente("PAC-FIRO-001");
            p1.setNombre("Firulais");
            p1.setEspecie("Perro");
            p1.setRaza("Labrador");
            p1.setEdad(3);
            p1.setCliente(c1);
            pacienteRepository.save(p1);

            Paciente p2 = new Paciente();
            p2.setCodigoPaciente("PAC-MICH-001");
            p2.setNombre("Michi");
            p2.setEspecie("Gato");
            p2.setRaza("Siamés");
            p2.setEdad(2);
            p2.setCliente(c2);
            pacienteRepository.save(p2);

            Paciente p3 = new Paciente();
            p3.setCodigoPaciente("PAC-TOBY-001");
            p3.setNombre("Toby");
            p3.setEspecie("Perro");
            p3.setRaza("Pastor Alemán");
            p3.setEdad(5);
            p3.setCliente(c1);
            pacienteRepository.save(p3);

            System.out.println("✅ Pacientes creados");
        }

        // === Categoría + Productos ===
        if (categoriaProductoRepository.count() == 0) {
            CategoriaProducto cat1 = new CategoriaProducto();
            cat1.setNombre("Medicamentos");
            cat1.setDescripcion("Medicamentos veterinarios");
            categoriaProductoRepository.save(cat1);

            CategoriaProducto cat2 = new CategoriaProducto();
            cat2.setNombre("Alimentos");
            cat2.setDescripcion("Alimentos para mascotas");
            categoriaProductoRepository.save(cat2);

            CategoriaProducto cat3 = new CategoriaProducto();
            cat3.setNombre("Servicios");
            cat3.setDescripcion("Servicios veterinarios");
            categoriaProductoRepository.save(cat3);

            System.out.println("✅ Categorías creadas");
        }

        if (productoRepository.count() == 0) {
            CategoriaProducto catMed = categoriaProductoRepository.findAll().get(0);
            CategoriaProducto catAlim = categoriaProductoRepository.findAll().get(1);
            CategoriaProducto catServ = categoriaProductoRepository.findAll().get(2);

            Producto prod1 = new Producto();
            prod1.setCodigoBarras("MED-001");
            prod1.setNombre("Amoxicilina 250mg");
            prod1.setDescripcion("Antibiótico de amplio espectro");
            prod1.setPrecioCompra(5.0);
            prod1.setPrecioVenta(12.0);
            prod1.setStockActual(100);
            prod1.setStockMinimo(10);
            prod1.setTipoInventario(TipoInventario.MEDICAMENTO);
            prod1.setCategoria(catMed);
            productoRepository.save(prod1);

            Producto prod2 = new Producto();
            prod2.setCodigoBarras("MED-002");
            prod2.setNombre("Desparasitante canino");
            prod2.setDescripcion("Desparasitante en tabletas");
            prod2.setPrecioCompra(3.0);
            prod2.setPrecioVenta(8.0);
            prod2.setStockActual(200);
            prod2.setStockMinimo(20);
            prod2.setTipoInventario(TipoInventario.MEDICAMENTO);
            prod2.setCategoria(catMed);
            productoRepository.save(prod2);

            Producto prod3 = new Producto();
            prod3.setCodigoBarras("ALI-001");
            prod3.setNombre("Dog Chow Adulto 15kg");
            prod3.setDescripcion("Alimento para perros adultos");
            prod3.setPrecioCompra(45.0);
            prod3.setPrecioVenta(75.0);
            prod3.setStockActual(50);
            prod3.setStockMinimo(5);
            prod3.setTipoInventario(TipoInventario.PETSHOP);
            prod3.setCategoria(catAlim);
            productoRepository.save(prod3);

            Producto prod4 = new Producto();
            prod4.setCodigoBarras("SERV-001");
            prod4.setNombre("Baño y Grooming");
            prod4.setDescripcion("Servicio de baño completo con corte");
            prod4.setPrecioCompra(0);
            prod4.setPrecioVenta(35.0);
            prod4.setStockActual(0);
            prod4.setStockMinimo(0);
            prod4.setTipoInventario(TipoInventario.SERVICIO);
            prod4.setCategoria(catServ);
            productoRepository.save(prod4);

            for (int i = 5; i <= 14; i++) {
                Producto px = new Producto();
                px.setCodigoBarras("PROD-" + i);
                px.setNombre("Producto Extra " + i);
                px.setDescripcion("Desc extra " + i);
                px.setPrecioCompra(10.0 + i);
                px.setPrecioVenta(20.0 + i);
                px.setStockActual(0);
                px.setStockMinimo(5);
                px.setTipoInventario((i % 2 == 0) ? TipoInventario.PETSHOP : TipoInventario.MEDICAMENTO);
                px.setCategoria((i % 2 == 0) ? catAlim : catMed);
                productoRepository.save(px);
            }

            System.out.println("✅ Productos creados");
        }

        // === Lotes y Movimientos de Inventario ===
        Usuario adminUser = usuarioRepository.findByEmail("quicanomorenojunior21072004@gmail.com").orElse(null);
        if (adminUser != null && loteRepository.count() == 0) {
            Producto prod1 = productoRepository.findById("MED-001").orElseThrow();
            Producto prod2 = productoRepository.findById("MED-002").orElseThrow();
            Producto prod3 = productoRepository.findById("ALI-001").orElseThrow();

            // Lote para prod1 (Amoxicilina) - Controlado (simulado)
            prod1.setIsControlado(true);
            productoRepository.save(prod1);

            Lote lote1 = new Lote();
            lote1.setProducto(prod1);
            lote1.setNumeroLote("L-AMX-2024-001");
            lote1.setFechaVencimiento(LocalDate.of(2025, 12, 31));
            lote1.setStockInicial(100);
            lote1.setStockActual(100);
            lote1.setCostoUnitario(5.0);
            lote1.setFechaIngreso(LocalDateTime.now().minusDays(30));
            loteRepository.save(lote1);

            MovimientoInventario mov1 = new MovimientoInventario();
            mov1.setProducto(prod1);
            mov1.setLote(lote1);
            mov1.setUsuario(adminUser);
            mov1.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
            mov1.setCantidad(100);
            mov1.setFechaHora(lote1.getFechaIngreso());
            movimientoRepository.save(mov1);

            // Lote para prod2 (Desparasitante)
            Lote lote2 = new Lote();
            lote2.setProducto(prod2);
            lote2.setNumeroLote("L-DES-2024-005");
            lote2.setFechaVencimiento(LocalDate.of(2026, 6, 15));
            lote2.setStockInicial(200);
            lote2.setStockActual(200);
            lote2.setCostoUnitario(3.0);
            lote2.setFechaIngreso(LocalDateTime.now().minusDays(15));
            loteRepository.save(lote2);

            MovimientoInventario mov2 = new MovimientoInventario();
            mov2.setProducto(prod2);
            mov2.setLote(lote2);
            mov2.setUsuario(adminUser);
            mov2.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
            mov2.setCantidad(200);
            mov2.setFechaHora(lote2.getFechaIngreso());
            movimientoRepository.save(mov2);

            // Lote para prod3 (Dog Chow - PetShop)
            Lote lote3 = new Lote();
            lote3.setProducto(prod3);
            lote3.setNumeroLote("L-DC-2024-010");
            lote3.setFechaVencimiento(LocalDate.of(2025, 8, 20));
            lote3.setStockInicial(50);
            lote3.setStockActual(50);
            lote3.setCostoUnitario(45.0);
            lote3.setFechaIngreso(LocalDateTime.now().minusDays(5));
            loteRepository.save(lote3);

            MovimientoInventario mov3 = new MovimientoInventario();
            mov3.setProducto(prod3);
            mov3.setLote(lote3);
            mov3.setUsuario(adminUser);
            mov3.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
            mov3.setCantidad(50);
            mov3.setFechaHora(lote3.getFechaIngreso());
            movimientoRepository.save(mov3);

            System.out.println("✅ Lotes y Movimientos iniciales creados");
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
            Paciente p1 = pacienteRepository.findById("PAC-FIRO-001").orElseThrow();
            Paciente p2 = pacienteRepository.findById("PAC-MICH-001").orElseThrow();
            Paciente p3 = pacienteRepository.findById("PAC-TOBY-001").orElseThrow();
            Veterinario v1 = veterinarioRepository.findById("12345678").orElseThrow(); // Cirugía / General
            Veterinario v2 = veterinarioRepository.findById("87654321").orElseThrow(); // Cardiología

            Consulta c1 = new Consulta();
            c1.setCodigoConsulta("CNS-HOY-001");
            c1.setFecha(LocalDate.now());
            c1.setMotivo("Vacunación Sextuple");
            c1.setPeso(12.5);
            c1.setObservaciones("Paciente alerta, mucosas rosadas.");
            c1.setDiagnostico("Sano. Vacunación preventiva.");
            c1.setTratamiento("Se aplica vacuna Sextuple SC. Próximo control en 1 año.");
            c1.setPaciente(p1);
            c1.setVeterinario(v1);
            consultaRepository.save(c1);

            Consulta c2 = new Consulta();
            c2.setCodigoConsulta("CNS-HOY-002");
            c2.setFecha(LocalDate.now());
            c2.setMotivo("Problema de piel, rascado constante");
            c2.setPeso(4.2);
            c2.setObservaciones("Alopecia en zona lumbar. Presencia de pulgas.");
            c2.setDiagnostico("Dermatitis Alérgica a la Picadura de Pulga (DAPP)");
            c2.setTratamiento("Bravecto 1 tab. Baño medicado con Clorhexidina cada 7 días.");
            c2.setPaciente(p2);
            c2.setVeterinario(v1);
            consultaRepository.save(c2);

            Consulta c3 = new Consulta();
            c3.setCodigoConsulta("CNS-HOY-003");
            c3.setFecha(LocalDate.now());
            c3.setMotivo("Control Carnet / Desparasitación");
            c3.setPeso(25.0);
            c3.setObservaciones("Paciente estable.");
            c3.setDiagnostico("Desparasitación de rutina");
            c3.setTratamiento("Drontal Plus 2.5 tabletas vía oral.");
            c3.setPaciente(p3);
            c3.setVeterinario(v2);
            consultaRepository.save(c3);

            // Una antigua para mostrar que el filtro "Hoy" funciona:
            Consulta c4 = new Consulta();
            c4.setCodigoConsulta("CNS-AYER-001");
            c4.setFecha(LocalDate.now().minusDays(1));
            c4.setMotivo("Vómitos esporádicos");
            c4.setPeso(12.0);
            c4.setObservaciones("Abdomen blando.");
            c4.setDiagnostico("Gastritis leve");
            c4.setTratamiento("Dieta blanda y Omeprazol 10mg.");
            c4.setPaciente(p1);
            c4.setVeterinario(v1);
            consultaRepository.save(c4);

            System.out.println("✅ Consultas (Mock HOY) creadas");
        }

        // === Población masiva de 20 mascotas y citas (NUEVO) ===
        poblarDatosPruebaAdicionales();

        System.out.println("🏁 DataLoader finalizado exitosamente");
    }

    private void poblarDatosPruebaAdicionales() {
        if (clienteRepository.count() > 2) {
            // Ya se poblaron los datos adicionales o ya hay suficientes
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
        String[] especies = { "Perro", "Gato", "Perro", "Gato", "Perro", "Perro", "Conejo", "Gato", "Perro", "Gato" };
        String[] razas = { "Pug", "Persa", "Beagle", "Siamés", "Golden", "Boxer", "Cabeza de León", "Angora", "Dálmata",
                "Siberiano" };

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
            cl.setDireccion("Calle de Pruebas " + (100 + i));
            clienteRepository.save(cl);

            // 2. Crear Mascota
            String codPaciente = "PAC-" + nombresMascotas[i].toUpperCase() + "-002";
            Paciente p = new Paciente();
            p.setCodigoPaciente(codPaciente);
            p.setNombre(nombresMascotas[i]);
            p.setEspecie(especies[i % especies.length]);
            p.setRaza(razas[i % razas.length]);
            p.setEdad(1 + (i % 8));
            p.setCliente(cl);
            pacienteRepository.save(p);

            // 3. Crear Cita
            // Distribuir entre el 09 y 15 de marzo de 2026
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
            // Turnos intercalados entre los dos veterinarios
            cita.setVeterinario(i % 2 == 0 ? v1 : v2);
            citaRepository.save(cita);
        }

        System.out.println("✅ 20 dueños, mascotas y citas creadas exitosamente");
    }
}
