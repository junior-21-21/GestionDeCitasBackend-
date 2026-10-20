package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.AjusteRequestDTO;
import com.farmacia.sistemaWeb.dto.CitaDTO;
import com.farmacia.sistemaWeb.dto.ConsultaDTO;
import com.farmacia.sistemaWeb.dto.ConsultaResponseDTO;
import com.farmacia.sistemaWeb.dto.PacienteDTO;
import com.farmacia.sistemaWeb.dto.PacienteResponseDTO;
import com.farmacia.sistemaWeb.dto.RegistroVacunaDTO;
import com.farmacia.sistemaWeb.dto.RegistroVacunaResponseDTO;
import com.farmacia.sistemaWeb.dto.VacunaDTO;
import com.farmacia.sistemaWeb.entity.*;
import com.farmacia.sistemaWeb.repository.*;
import com.farmacia.sistemaWeb.service.HistorialDemoDatos.Caso;
import com.farmacia.sistemaWeb.service.HistorialDemoDatos.Pres;
import com.farmacia.sistemaWeb.service.HistorialDemoDatos.Prod;
import com.farmacia.sistemaWeb.service.HistorialDemoDatos.RazaDemo;
import com.farmacia.sistemaWeb.service.HistorialDemoDatos.Tipo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.sql.Connection;
import java.text.Normalizer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.farmacia.sistemaWeb.service.HistorialDemoDatos.*;

/**
 * Generador de historial de demostración: simula 30 días de uso diario de la clínica (los 30 días que terminan ayer).
 * <p>
 * Todo se crea con los servicios del sistema (compras, ventas FEFO, ajustes, bajas, cobros, vacunas, consultas),
 * de modo que se cumplen las reglas del inventario (stock = suma de lotes, kardex por cada movimiento, costo promedio,
 * presentaciones). Después de cada operación se "retrocede" su fecha a la hora simulada. Los días se simulan en orden
 * cronológico y, dentro del día, los eventos en orden de hora, así el saldo del kardex se lee correcto en el tiempo.
 * <p>
 * Lo creado se anota en la tabla auxiliar {@value #TABLA} (fuera del modelo JPA) para poder borrarlo exactamente con
 * {@link #eliminar()}. La marca de "ya generado" es el producto {@link HistorialDemoDatos#CODIGO_MARCA}.
 */
@Service
public class HistorialDemoService {

    private static final Logger log = LoggerFactory.getLogger(HistorialDemoService.class);

    static final String TABLA = "historial_demo";
    private static final long SEMILLA = 20260912L;
    private static final DateTimeFormatter COD_FECHA = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** La operación no se puede hacer en el estado actual (ya generado, en curso, datos reales mezclados...). */
    public static class ConflictoException extends RuntimeException {
        public ConflictoException(String mensaje) {
            super(mensaje);
        }
    }

    @PersistenceContext private EntityManager em;
    @Autowired private PlatformTransactionManager txManager;
    @Autowired private DataSource dataSource;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private ProductoService productoService;
    @Autowired private CompraService compraService;
    @Autowired private VentaService ventaService;
    @Autowired private InventarioService inventarioService;
    @Autowired private CobroConsultaService cobroConsultaService;
    @Autowired private RegistroVacunaService registroVacunaService;
    @Autowired private RecetaMedicaService recetaMedicaService;
    @Autowired private ConsultaService consultaService;
    @Autowired private CitaService citaService;
    @Autowired private ProveedorService proveedorService;
    @Autowired private VacunaService vacunaService;
    @Autowired private EspecieRazaService especieRazaService;
    @Autowired private PacienteService pacienteService;

    @Autowired private ProductoRepository productoRepository;
    @Autowired private PresentacionRepository presentacionRepository;
    @Autowired private LoteRepository loteRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private TelefonoClienteRepository telefonoClienteRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private CitaRepository citaRepository;
    @Autowired private ConsultaRepository consultaRepository;
    @Autowired private VeterinarioRepository veterinarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private EspecialidadRepository especialidadRepository;
    @Autowired private EspecieRepository especieRepository;
    @Autowired private RazaRepository razaRepository;
    @Autowired private AsistenciaDiariaRepository asistenciaRepository;
    @Autowired private HorarioTrabajadorRepository horarioRepository;
    @Autowired private ProveedorRepository proveedorRepository;

    @Value("${app.citas.hora-apertura:08:00}")
    private String horaApertura;

    @Value("${app.citas.hora-cierre:20:00}")
    private String horaCierre;

    /** Multiplica las ventas de mostrador por dia (1 = volumen normal). */
    @Value("${app.demo.historial.factor-ventas:1}")
    private int factorVentas;

    private final AtomicBoolean enCurso = new AtomicBoolean(false);

    // ─────────────────────────── API pública ───────────────────────────

    /** true si el historial de demostración ya existe en esta base de datos. */
    public boolean yaGenerado() {
        return productoRepository.findByCodigo(CODIGO_MARCA).isPresent();
    }

    /**
     * Genera el historial. Lanza {@link ConflictoException} si ya existe o si otra generación está en curso.
     * @return resumen con los totales creados
     */
    public Map<String, Object> generar() {
        if (!enCurso.compareAndSet(false, true)) {
            throw new ConflictoException("Ya hay una generación o eliminación del historial de demostración en curso");
        }
        SecurityContext previo = SecurityContextHolder.getContext();
        try {
            if (yaGenerado()) {
                throw new ConflictoException("El historial de demostración ya fue generado en esta base de datos "
                        + "(existe el producto " + CODIGO_MARCA + "). No se volvió a generar.");
            }
            asegurarTablaRegistro();
            return new Corrida().ejecutar();
        } finally {
            SecurityContextHolder.setContext(previo);
            enCurso.set(false);
        }
    }

    // ─────────────────────────── Tabla de registro ───────────────────────────

    private boolean esSqlServer() {
        try (Connection c = dataSource.getConnection()) {
            String p = c.getMetaData().getDatabaseProductName();
            return p != null && p.toLowerCase(Locale.ROOT).contains("microsoft");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean existeTablaRegistro() {
        try {
            new JdbcTemplate(dataSource).queryForObject("SELECT COUNT(*) FROM " + TABLA, Long.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Tabla auxiliar (tipo, ref) con lo generado. No es una entidad: ddl-auto=validate no la exige. */
    private void asegurarTablaRegistro() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        if (esSqlServer()) {
            jdbc.execute("IF OBJECT_ID('" + TABLA + "', 'U') IS NULL CREATE TABLE " + TABLA
                    + " (tipo VARCHAR(20) NOT NULL, ref VARCHAR(60) NOT NULL)");
        } else {
            jdbc.execute("CREATE TABLE IF NOT EXISTS " + TABLA + " (tipo VARCHAR(20) NOT NULL, ref VARCHAR(60) NOT NULL)");
        }
        // Restos de una base recreada (ddl-auto=create): el registro no apunta a nada que exista
        Long filas = jdbc.queryForObject("SELECT COUNT(*) FROM " + TABLA, Long.class);
        if (filas != null && filas > 0) {
            Long vivos = jdbc.queryForObject("SELECT COUNT(*) FROM productos "
                    + "WHERE codigo LIKE '" + HistorialDemoDatos.PREFIJO + "%'", Long.class);
            if (vivos == null || vivos == 0) {
                jdbc.update("DELETE FROM " + TABLA);
            }
        }
    }

    // ─────────────────────────── Utilidades comunes ───────────────────────────

    private void comoUsuario(Usuario u) {
        if (u == null) {
            SecurityContextHolder.clearContext();
            return;
        }
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        String rol = u.getRol() != null && u.getRol().getNombre() != null ? u.getRol().getNombre().name() : "ADMIN";
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(u.getEmail(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
        SecurityContextHolder.setContext(ctx);
    }

    private <T> T enTx(Usuario como, Supplier<T> trabajo) {
        comoUsuario(como);
        return new TransactionTemplate(txManager).execute(st -> trabajo.get());
    }

    /** Pone la fecha simulada a los movimientos de kardex de un documento (opcionalmente solo entradas o salidas). */
    private void fecharKardex(String docTipo, String docId, LocalDateTime t, String soloMovimiento) {
        em.flush();
        String jpql = "UPDATE Kardex k SET k.fechaHora = :t WHERE k.documentoTipo = :dt AND k.documentoId = :di"
                + (soloMovimiento != null ? " AND k.movimiento = :mv" : "");
        var q = em.createQuery(jpql).setParameter("t", t).setParameter("dt", docTipo).setParameter("di", docId);
        if (soloMovimiento != null) q.setParameter("mv", soloMovimiento);
        q.executeUpdate();
    }

    private static String sinTildes(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    static int digitoRuc(String diez) {
        int[] pesos = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int suma = 0;
        for (int i = 0; i < 10; i++) suma += (diez.charAt(i) - '0') * pesos[i];
        int r = 11 - suma % 11;
        return r == 10 ? 0 : (r == 11 ? 1 : r);
    }

    // ═════════════════════════════ Una corrida ═════════════════════════════

    private final class Corrida {

        final Random rnd = new Random(SEMILLA);
        final Map<String, Integer> cuenta = new LinkedHashMap<>();
        final List<String[]> registroPendiente = new ArrayList<>();
        final Set<String> barrasUsadas = new HashSet<>();

        LocalDate hoy;
        LocalDate inicio;
        LocalDate fin;
        LocalTime apertura;
        LocalTime cierre;

        Usuario admin;
        Usuario recep;
        Usuario encargado;
        Usuario groomer;
        final List<Vet> vets = new ArrayList<>();
        final List<String> personalCreado = new ArrayList<>();
        String claveDemo;

        /** Turnos: id de usuario → día → {inicio, fin}. */
        final Map<Long, Map<DayOfWeek, LocalTime[]>> turnos = new HashMap<>();

        final Map<String, Prod> def = new LinkedHashMap<>();
        final Map<String, Long> prodId = new HashMap<>();
        final Map<String, Long[]> presId = new HashMap<>();
        final Map<Integer, Long> proveedorId = new HashMap<>();
        final Map<Integer, Integer> facturaProveedor = new HashMap<>();
        final Map<String, Long> vacunaPorProducto = new HashMap<>();
        final Map<String, String> nombreVacuna = new HashMap<>();
        final List<Cli> clientes = new ArrayList<>();
        final List<Pac> pacientes = new ArrayList<>();

        Long loteQueVence;
        LocalDate venceLote;
        double ingresosVentas;
        double ingresosConsultas;
        final Set<String> tardanzas = new HashSet<>();

        record Vet(Veterinario v, Usuario u, Tipo esp, int idx) {
        }

        record Cli(String dni, String nombre) {
        }

        final class Pac {
            String codigo;
            String nombre;
            String especie;
            String genero;
            LocalDate nacimiento;
            double peso;
            boolean frecuente;
            final Set<String> vacunas = new HashSet<>();
            boolean operado;
            boolean derma;
            int visitas;
            LocalDate ultima;
        }

        private final class Evento {
            final LocalDateTime t;
            final int orden;
            final String que;
            final Runnable accion;

            Evento(LocalDateTime t, int orden, String que, Runnable accion) {
                this.t = t;
                this.orden = orden;
                this.que = que;
                this.accion = accion;
            }
        }

        int secuencia;
        LocalDate diaActual;

        // ─────────── utilidades aleatorias ───────────

        int entre(int a, int b) {
            return a + rnd.nextInt(b - a + 1);
        }

        double entre(double a, double b) {
            return a + rnd.nextDouble() * (b - a);
        }

        <T> T uno(List<T> l) {
            return l.get(rnd.nextInt(l.size()));
        }

        String uno(String[] a) {
            return a[rnd.nextInt(a.length)];
        }

        <T> T ponderado(List<T> l, java.util.function.ToDoubleFunction<T> peso) {
            double total = 0;
            for (T x : l) total += Math.max(0, peso.applyAsDouble(x));
            if (total <= 0) return null;
            double r = rnd.nextDouble() * total;
            for (T x : l) {
                r -= Math.max(0, peso.applyAsDouble(x));
                if (r <= 0) return x;
            }
            return l.get(l.size() - 1);
        }

        void contar(String clave, int n) {
            cuenta.merge(clave, n, Integer::sum);
        }

        void registrar(String tipo, Object ref) {
            if (ref != null) registroPendiente.add(new String[]{tipo, String.valueOf(ref)});
        }

        void volcarRegistro() {
            if (registroPendiente.isEmpty()) return;
            List<Object[]> filas = new ArrayList<>();
            for (String[] r : registroPendiente) filas.add(new Object[]{r[0], r[1]});
            new JdbcTemplate(dataSource).batchUpdate("INSERT INTO " + TABLA + " (tipo, ref) VALUES (?, ?)", filas);
            registroPendiente.clear();
        }

        LocalDateTime en(LocalDate d, LocalTime h) {
            return LocalDateTime.of(d, h).withSecond(rnd.nextInt(60));
        }

        // ═════════════════════════════ Flujo principal ═════════════════════════════

        Map<String, Object> ejecutar() {
            hoy = LocalDate.now();
            inicio = hoy.minusDays(30);
            fin = hoy.minusDays(1);
            apertura = LocalTime.parse(horaApertura);
            cierre = LocalTime.parse(horaCierre);
            long t0 = System.currentTimeMillis();
            log.info("Historial de demostración: generando 30 días de uso simulado ({} a {})...", inicio, fin);

            prepararPersonal();
            volcarRegistro();
            prepararCategoriasYProveedores();
            prepararVacunas();
            prepararProductos();
            volcarRegistro();
            prepararClientesYPacientes();
            prepararHorarios();
            volcarRegistro();

            // Tardanzas: tres entradas tarde repartidas en el mes
            List<Usuario> staff = personalConTurno();
            for (int i = 0; i < 3 && !staff.isEmpty(); i++) {
                tardanzas.add(uno(staff).getId() + "|" + entre(2, 28));
            }

            int idx = 0;
            for (LocalDate d = inicio; !d.isAfter(fin); d = d.plusDays(1), idx++) {
                simularDia(d, idx);
                volcarRegistro();
            }
            citasFuturas();
            volcarRegistro();
            comoUsuario(null);

            Map<String, Object> resumen = resumen(System.currentTimeMillis() - t0);
            StringBuilder sb = new StringBuilder("\n══════ Historial de demostración generado ══════\n");
            resumen.forEach((k, v) -> {
                if (!"contrasenaPersonalDemo".equals(k)) sb.append(String.format("  %-26s %s%n", k, v));
            });
            sb.append("═══════════════════════════════════════════════");
            log.info(sb.toString());
            return resumen;
        }

        Map<String, Object> resumen(long ms) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("desde", inicio.toString());
            r.put("hasta", fin.toString());
            r.put("personalCreado", personalCreado.isEmpty() ? "ninguno (se usó el personal existente)" : String.join(", ", personalCreado));
            String[] claves = {"clientes", "pacientes", "proveedores", "productos", "compras", "lotes", "ventas", "ventasAnuladas",
                    "citas", "citasAtendidas", "citasCanceladas", "citasNoAsistio", "citasProgramadas", "consultas", "recetas",
                    "vacunasAplicadas", "cobros", "cobrosPagados", "cobrosPendientes", "ajustes", "bajasPorVencimiento",
                    "asistencias", "horarios", "operacionesOmitidas"};
            cuenta.put("cobrosPendientes", cuenta.getOrDefault("cobros", 0) - cuenta.getOrDefault("cobrosPagados", 0));
            for (String c : claves) r.put(c, cuenta.getOrDefault(c, 0));
            Long kardex = em.createQuery("SELECT COUNT(k) FROM Kardex k WHERE k.producto.codigo LIKE :p", Long.class)
                    .setParameter("p", PREFIJO + "%").getSingleResult();
            r.put("movimientosKardex", kardex);
            r.put("ingresosVentas", BigDecimal.valueOf(ingresosVentas).setScale(2, RoundingMode.HALF_UP));
            r.put("ingresosConsultas", BigDecimal.valueOf(ingresosConsultas).setScale(2, RoundingMode.HALF_UP));
            r.put("segundos", ms / 1000);
            if (claveDemo != null) {
                r.put("contrasenaPersonalDemo", claveDemo);
            }
            return r;
        }

        // ═════════════════════════════ Preparación ═════════════════════════════

        List<Usuario> porRol(Rol.NombreRol rol) {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.rol.nombre = :r AND u.habilitada = true ORDER BY u.id", Usuario.class)
                    .setParameter("r", rol).getResultList();
        }

        String clave() {
            if (claveDemo == null) {
                SecureRandom sr = new SecureRandom();
                String abc = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 14; i++) sb.append(abc.charAt(sr.nextInt(abc.length())));
                sb.insert(5, '-').insert(11, '!');
                claveDemo = sb.toString();
            }
            return claveDemo;
        }

        Usuario crearUsuario(String nombres, String apellidos, String email, Rol.NombreRol nombreRol) {
            Optional<Usuario> ya = usuarioRepository.findByEmail(email);
            if (ya.isPresent()) return ya.get();
            Rol rol = rolRepository.findByNombre(nombreRol).orElseThrow(() -> new IllegalStateException("Falta el rol " + nombreRol));
            Usuario u = new Usuario();
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(clave()));
            u.setNombres(nombres);
            u.setApellidos(apellidos);
            u.setRol(rol);
            u.setHabilitada(true);
            u.setCuentaBloqueada(false);
            u.setIntentosFallidos(0);
            u = usuarioRepository.save(u);
            registrar("USUARIO", u.getId());
            personalCreado.add(email + " (" + nombreRol + ")");
            return u;
        }

        Especialidad especialidad(String nombre) {
            List<Especialidad> l = em.createQuery("SELECT e FROM Especialidad e WHERE LOWER(e.nombre) = :n ORDER BY e.id", Especialidad.class)
                    .setParameter("n", nombre.toLowerCase(Locale.ROOT)).getResultList();
            if (!l.isEmpty()) return l.get(0);
            Especialidad e = new Especialidad();
            e.setNombre(nombre);
            e = especialidadRepository.save(e);
            registrar("ESPECIALIDAD", e.getId());
            return e;
        }

        Tipo tipoDe(Especialidad e) {
            String n = e == null || e.getNombre() == null ? "" : sinTildes(e.getNombre()).toLowerCase(Locale.ROOT);
            if (n.contains("ciru")) return Tipo.CIRUGIA;
            if (n.contains("derma")) return Tipo.DERMA;
            return Tipo.GENERAL;
        }

        void prepararPersonal() {
            enTx(null, () -> {
                List<Usuario> admins = porRol(Rol.NombreRol.ADMIN);
                admin = admins.isEmpty() ? null : admins.get(0);
                List<Usuario> l = porRol(Rol.NombreRol.RECEPCIONISTA);
                recep = !l.isEmpty() ? l.get(0) : crearUsuario(RECEPCIONISTA[0], RECEPCIONISTA[1], RECEPCIONISTA[2], Rol.NombreRol.RECEPCIONISTA);
                l = porRol(Rol.NombreRol.ENCARGADO_ALMACEN);
                encargado = !l.isEmpty() ? l.get(0) : crearUsuario(ENCARGADO[0], ENCARGADO[1], ENCARGADO[2], Rol.NombreRol.ENCARGADO_ALMACEN);
                l = porRol(Rol.NombreRol.GROOMER);
                groomer = !l.isEmpty() ? l.get(0) : crearUsuario(GROOMER[0], GROOMER[1], GROOMER[2], Rol.NombreRol.GROOMER);

                List<Veterinario> existentes = em.createQuery(
                        "SELECT v FROM Veterinario v JOIN FETCH v.usuario u LEFT JOIN FETCH v.especialidad WHERE u.habilitada = true ORDER BY v.dni",
                        Veterinario.class).getResultList();
                for (Veterinario v : existentes) {
                    if (vets.size() >= 3) break;
                    vets.add(new Vet(v, v.getUsuario(), tipoDe(v.getEspecialidad()), vets.size()));
                }
                for (String[] d : VETERINARIOS) {
                    if (vets.size() >= 3) break;
                    if (vets.stream().anyMatch(x -> d[2].equalsIgnoreCase(x.v().getCorreo()))) continue;
                    Usuario u = crearUsuario(d[0], d[1], d[2], Rol.NombreRol.VETERINARIO);
                    Veterinario v = veterinarioRepository.findByCorreo(d[2]).orElse(null);
                    if (v == null) {
                        String dni = d[3];
                        while (veterinarioRepository.existsById(dni)) dni = String.valueOf(Long.parseLong(dni) + 7);
                        v = new Veterinario();
                        v.setDni(dni);
                        v.setNombres(d[0]);
                        v.setApellidos(d[1]);
                        v.setCorreo(d[2]);
                        v.setCelular(d[5]);
                        v.setEspecialidad(especialidad(d[4]));
                        v.setUsuario(u);
                        v = veterinarioRepository.save(v);
                        registrar("VETERINARIO", v.getDni());
                    }
                    vets.add(new Vet(v, u, tipoDe(v.getEspecialidad()), vets.size()));
                }
                return null;
            });
            if (claveDemo != null) {
                log.warn("Historial de demostración: se creó personal de prueba ({}). Contraseña común (se muestra una sola vez): {}",
                        String.join(", ", personalCreado), claveDemo);
            }
            // Turnos semanales
            for (Vet v : vets) {
                Map<DayOfWeek, LocalTime[]> m = new EnumMap<>(DayOfWeek.class);
                switch (v.idx()) {
                    case 0 -> {
                        for (DayOfWeek d : lunesASabado()) m.put(d, horas("08:00", "16:00"));
                        m.put(DayOfWeek.SUNDAY, horas("09:00", "13:00"));
                    }
                    case 1 -> {
                        for (DayOfWeek d : lunesASabado()) m.put(d, horas("12:00", "20:00"));
                    }
                    default -> {
                        for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)) m.put(d, horas("09:00", "17:00"));
                        m.put(DayOfWeek.TUESDAY, horas("14:00", "20:00"));
                        m.put(DayOfWeek.SATURDAY, horas("08:00", "14:00"));
                    }
                }
                turnos.put(v.u().getId(), m);
            }
            Map<DayOfWeek, LocalTime[]> mr = new EnumMap<>(DayOfWeek.class);
            for (DayOfWeek d : lunesASabado()) mr.put(d, horas("08:00", "16:00"));
            mr.put(DayOfWeek.SUNDAY, horas("09:00", "13:00"));
            turnos.put(recep.getId(), mr);
            Map<DayOfWeek, LocalTime[]> me = new EnumMap<>(DayOfWeek.class);
            for (DayOfWeek d : lunesASabado()) me.put(d, horas("11:00", "20:00"));
            turnos.put(encargado.getId(), me);
            Map<DayOfWeek, LocalTime[]> mg = new EnumMap<>(DayOfWeek.class);
            for (DayOfWeek d : List.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)) {
                mg.put(d, horas("09:00", "17:00"));
            }
            mg.put(DayOfWeek.SUNDAY, horas("09:00", "13:00"));
            turnos.putIfAbsent(groomer.getId(), mg);
        }

        List<DayOfWeek> lunesASabado() {
            return List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
        }

        LocalTime[] horas(String a, String b) {
            LocalTime i = LocalTime.parse(a);
            LocalTime f = LocalTime.parse(b);
            // Dentro del horario de atención configurado
            if (i.isBefore(apertura)) i = apertura;
            if (f.isAfter(cierre)) f = cierre;
            return new LocalTime[]{i, f};
        }

        List<Usuario> personalConTurno() {
            List<Usuario> l = new ArrayList<>();
            for (Vet v : vets) l.add(v.u());
            l.add(recep);
            if (!l.contains(encargado)) l.add(encargado);
            if (!l.contains(groomer)) l.add(groomer);
            return l;
        }

        LocalTime[] turno(Usuario u, LocalDate d) {
            Map<DayOfWeek, LocalTime[]> m = turnos.get(u.getId());
            return m == null ? null : m.get(d.getDayOfWeek());
        }

        boolean enTurno(Usuario u, LocalDateTime t) {
            LocalTime[] h = turno(u, t.toLocalDate());
            return h != null && !t.toLocalTime().isBefore(h[0]) && t.toLocalTime().isBefore(h[1]);
        }

        /** Quién atiende la caja a esa hora (recepción por la mañana, encargado por la tarde). */
        Usuario cajero(LocalDateTime t) {
            List<Usuario> l = new ArrayList<>();
            if (enTurno(recep, t)) l.add(recep);
            if (enTurno(encargado, t)) l.add(encargado);
            if (l.isEmpty()) return admin != null ? admin : recep;
            return uno(l);
        }

        Usuario almacen() {
            return encargado != null ? encargado : admin;
        }

        void prepararCategoriasYProveedores() {
            enTx(null, () -> {
                List<Categoria> todas = categoriaRepository.findAll();
                for (String[] c : CATEGORIAS) {
                    boolean existe = todas.stream().anyMatch(x -> x.getNombre() != null
                            && sinTildes(x.getNombre()).equalsIgnoreCase(sinTildes(c[0])));
                    if (!existe) {
                        Categoria n = new Categoria();
                        n.setNombre(c[0]);
                        n.setDescripcion(c[1]);
                        n = categoriaRepository.save(n);
                        registrar("CATEGORIA", n.getId());
                    }
                }
                for (int i = 0; i < PROVEEDORES.length; i++) {
                    String[] p = PROVEEDORES[i];
                    String ruc = p[1] + digitoRuc(p[1]);
                    Optional<Proveedor> ya = proveedorRepository.findByRuc(ruc);
                    if (ya.isPresent()) {
                        proveedorId.put(i, ya.get().getId());
                        continue;
                    }
                    Proveedor n = new Proveedor();
                    n.setRuc(ruc);
                    n.setRazonSocial(p[0]);
                    n.setTelefono(p[2]);
                    n.setDireccion(p[3]);
                    n = proveedorService.crear(n);
                    proveedorId.put(i, n.getId());
                    registrar("PROVEEDOR", n.getId());
                    contar("proveedores", 1);
                    facturaProveedor.put(i, entre(1200, 8800));
                }
                return null;
            });
        }

        Categoria categoria(String nombre) {
            return categoriaRepository.findAll().stream()
                    .filter(x -> x.getNombre() != null && sinTildes(x.getNombre()).equalsIgnoreCase(sinTildes(nombre)))
                    .findFirst().orElseThrow();
        }

        void prepararVacunas() {
            String[][] vac = {
                    {"PZ-VAC-001", "Séxtuple canina (DHPPi-L)", "Zoetis", "OBLIGATORIA", "Perro",
                            "Distemper, hepatitis, adenovirus, parainfluenza, parvovirus y leptospira"},
                    {"PZ-VAC-002", "Antirrábica", "MSD Salud Animal", "OBLIGATORIA", "Perro,Gato", "Rabia, refuerzo anual"},
                    {"PZ-VAC-003", "Triple felina", "Boehringer Ingelheim", "OBLIGATORIA", "Gato",
                            "Panleucopenia, rinotraqueítis y calicivirus"},
                    {"PZ-VAC-004", "Tos de las perreras (KC)", "Zoetis", "OPCIONAL", "Perro", "Bordetella y parainfluenza, intranasal"},
            };
            enTx(null, () -> {
                for (String[] v : vac) {
                    Optional<Vacuna> ya = vacunaService.listarTodas().stream()
                            .filter(x -> x.getNombre() != null && sinTildes(x.getNombre()).equalsIgnoreCase(sinTildes(v[1])))
                            .findFirst();
                    Long id;
                    if (ya.isPresent()) {
                        id = ya.get().getId();
                        if (!ya.get().isActiva()) vacunaService.cambiarEstado(id, true);
                    } else {
                        VacunaDTO dto = new VacunaDTO();
                        dto.setNombre(v[1]);
                        dto.setFabricante(v[2]);
                        dto.setTipo(v[3]);
                        dto.setDescripcion(v[5]);
                        dto.setPeriodicidadMeses(12);
                        List<Long> especies = new ArrayList<>();
                        for (String e : v[4].split(",")) {
                            especies.add(especieRazaService.obtenerOCrearRaza(e, "Mestizo").getEspecie().getId());
                        }
                        dto.setEspecieIds(especies);
                        id = vacunaService.crear(dto).getId();
                        registrar("VACUNA", id);
                    }
                    vacunaPorProducto.put(v[0], id);
                    nombreVacuna.put(v[0], v[1]);
                }
                return null;
            });
        }

        String ean13() {
            while (true) {
                StringBuilder sb = new StringBuilder("775");
                for (int i = 0; i < 9; i++) sb.append(rnd.nextInt(10));
                String c = sb.append(CatalogoService.digitoControlEan13(sb.toString())).toString();
                if (barrasUsadas.add(c) && !presentacionRepository.existsByCodigoBarras(c)) return c;
            }
        }

        void prepararProductos() {
            for (Prod p : PRODUCTOS) {
                def.put(p.codigo(), p);
                enTx(almacen(), () -> {
                    Optional<Producto> ya = productoRepository.findByCodigo(p.codigo());
                    if (ya.isPresent()) {
                        throw new ConflictoException("Ya existe un producto con el código " + p.codigo()
                                + ": el historial de demostración parece generado a medias. Use DELETE /api/demo/historial.");
                    }
                    Producto pr = new Producto();
                    pr.setCodigo(p.codigo());
                    pr.setNombre(p.nombre());
                    pr.setDescripcion(p.descripcion());
                    pr.setClase(p.clase());
                    pr.setUnidadBase(p.unidad());
                    pr.setCategoria(categoria(p.categoria()));
                    pr.setStockMinimo(p.stockMinimo());
                    BigDecimal costo = new BigDecimal(p.costoCompra());
                    int factorCompra = p.pres().get(p.compraIdx()).factor();
                    pr.setPrecioCompra(costo.divide(BigDecimal.valueOf(factorCompra), 2, RoundingMode.HALF_UP));
                    pr.setPrecioVenta(new BigDecimal(p.pres().get(0).precio()));
                    List<Presentacion> pres = new ArrayList<>();
                    for (Pres d : p.pres()) {
                        Presentacion x = new Presentacion();
                        x.setNombre(d.nombre());
                        x.setFactor(d.factor());
                        x.setPrecioVenta(new BigDecimal(d.precio()));
                        x.setVendible(d.vendible());
                        x.setComprable(d.comprable());
                        x.setActivo(true);
                        boolean granel = d.factor() == 1 && (p.unidad() == UnidadBase.GRAMO || p.unidad() == UnidadBase.ML);
                        if (p.clase() != ClaseProducto.SERVICIO && !granel) x.setCodigoBarras(ean13());
                        pres.add(x);
                    }
                    pr.setPresentaciones(pres);
                    Producto g = productoService.crear(pr);
                    prodId.put(p.codigo(), g.getId());
                    List<Presentacion> guardadas = presentacionRepository.findByProductoIdOrdenadas(g.getId());
                    Long[] ids = new Long[p.pres().size()];
                    for (int i = 0; i < p.pres().size(); i++) {
                        String nombre = p.pres().get(i).nombre();
                        ids[i] = guardadas.stream().filter(x -> x.getNombre().equals(nombre)).findFirst().orElseThrow().getId();
                    }
                    presId.put(p.codigo(), ids);
                    registrar("PRODUCTO", g.getId());
                    contar("productos", 1);
                    return null;
                });
            }
        }

        void prepararClientesYPacientes() {
            Set<String> dnis = new HashSet<>();
            Set<String> correos = new HashSet<>();
            String[] dominios = {"gmail.com", "gmail.com", "gmail.com", "hotmail.com", "outlook.com", "yahoo.es"};
            enTx(null, () -> {
                for (int i = 0; i < 60; i++) {
                    boolean mujer = rnd.nextBoolean();
                    String nombre = uno(mujer ? NOMBRES_F : NOMBRES_M);
                    String ap1 = uno(APELLIDOS);
                    String ap2;
                    do ap2 = uno(APELLIDOS); while (ap2.equals(ap1));
                    String dni;
                    do dni = String.format("%08d", entre(10_000_000, 79_999_999));
                    while (!dnis.add(dni) || clienteRepository.existsById(dni));
                    String base = sinTildes(nombre.split(" ")[0] + "." + ap1).toLowerCase(Locale.ROOT);
                    String correo;
                    do correo = base + entre(1, 99) + "@" + uno(dominios);
                    while (!correos.add(correo) || em.createQuery("SELECT COUNT(c) FROM Cliente c WHERE c.email = :e", Long.class)
                            .setParameter("e", correo).getSingleResult() > 0);
                    Cliente c = new Cliente();
                    c.setDni(dni);
                    c.setTipoDocumento(Cliente.TipoDocumento.DNI);
                    c.setNombres(nombre);
                    c.setApellidos(ap1 + " " + ap2);
                    c.setTelefono("9" + entre(10_000_000, 99_999_999));
                    c.setCalle(uno(CALLES));
                    c.setNumero(String.valueOf(entre(1, 48) * 10 + entre(0, 9)));
                    c.setDistrito(uno(DISTRITOS));
                    c.setProvincia("Lima");
                    c.setEmail(correo);
                    c.setPuntosFidelidad(0);
                    c = clienteRepository.save(c);
                    if (rnd.nextDouble() < 0.3) {
                        TelefonoCliente t = new TelefonoCliente();
                        boolean casa = rnd.nextBoolean();
                        t.setNumero(casa ? "01" + entre(2_000_000, 7_999_999) : "9" + entre(10_000_000, 99_999_999));
                        t.setTipo(casa ? TelefonoCliente.TipoTelefono.CASA : TelefonoCliente.TipoTelefono.CELULAR);
                        t.setCliente(c);
                        telefonoClienteRepository.save(t);
                    }
                    clientes.add(new Cli(dni, nombre + " " + ap1 + " " + ap2));
                    registrar("CLIENTE", dni);
                    contar("clientes", 1);
                }
                return null;
            });

            // 85 mascotas: 51 perros, 30 gatos, 2 conejos y 2 hámsteres
            List<String> especies = new ArrayList<>();
            for (int i = 0; i < 51; i++) especies.add("Perro");
            for (int i = 0; i < 30; i++) especies.add("Gato");
            especies.addAll(List.of("Conejo", "Conejo", "Hamster", "Hamster"));
            Collections.shuffle(especies, rnd);
            List<Cli> duenos = new ArrayList<>(clientes);
            List<Cli> segundos = new ArrayList<>(clientes);
            Collections.shuffle(segundos, rnd);
            for (int i = 0; i < 25; i++) duenos.add(segundos.get(i));
            Map<String, Set<String>> nombresPorDueno = new HashMap<>();
            for (int i = 0; i < especies.size(); i++) {
                String especie = especies.get(i);
                Cli dueno = duenos.get(i);
                List<RazaDemo> razas = RAZAS.stream().filter(r -> r.especie().equals(especie)).toList();
                RazaDemo rz = ponderado(razas, RazaDemo::peso);
                String[] lista = especie.equals("Perro") ? NOMBRES_PERRO : especie.equals("Gato") ? NOMBRES_GATO : NOMBRES_OTROS;
                Set<String> usados = nombresPorDueno.computeIfAbsent(dueno.dni(), k -> new HashSet<>());
                String nombre;
                do nombre = uno(lista); while (!usados.add(nombre));
                int edadDias;
                double q = rnd.nextDouble();
                if (especie.equals("Hamster")) edadDias = entre(90, 600);
                else if (especie.equals("Conejo")) edadDias = entre(200, 1500);
                else if (q < 0.12) edadDias = entre(60, 200);
                else if (q < 0.42) edadDias = entre(365, 3 * 365);
                else if (q < 0.82) edadDias = entre(3 * 365, 9 * 365);
                else edadDias = entre(9 * 365, 14 * 365);
                double pesoAdulto = entre(rz.pesoMin(), rz.pesoMax());
                double factorEdad = edadDias < 300 ? 0.25 + 0.75 * edadDias / 300.0 : 1.0;
                double peso = pesoAdulto * factorEdad;
                peso = especie.equals("Hamster") ? Math.round(peso * 1000) / 1000.0 : Math.round(peso * 10) / 10.0;

                Pac p = new Pac();
                p.nombre = nombre;
                p.especie = especie;
                p.genero = rnd.nextBoolean() ? "Macho" : "Hembra";
                p.nacimiento = hoy.minusDays(edadDias);
                p.peso = peso;
                final String fEspecie = especie;
                final String fRaza = rz.raza();
                final String fNombre = nombre;
                final double fPeso = peso;
                enTx(recep, () -> {
                    Long especieId = especieRepository.findByNombreIgnoreCase(fEspecie).map(Especie::getId).orElse(null);
                    boolean razaNueva = especieId == null
                            || razaRepository.findByNombreIgnoreCaseAndEspecieId(fRaza, especieId).isEmpty();
                    Raza raza = especieRazaService.obtenerOCrearRaza(fEspecie, fRaza);
                    if (razaNueva) registrar("RAZA", raza.getId());
                    PacienteDTO dto = new PacienteDTO();
                    dto.setNombre(fNombre);
                    dto.setRazaId(raza.getId());
                    dto.setFechaNacimiento(p.nacimiento);
                    dto.setPeso(fPeso);
                    dto.setGenero(p.genero);
                    dto.setClienteDni(dueno.dni());
                    PacienteResponseDTO r = pacienteService.registrarPaciente(dto);
                    p.codigo = r.getCodigoPaciente();
                    registrar("PACIENTE", p.codigo);
                    return null;
                });
                pacientes.add(p);
                contar("pacientes", 1);
            }
            // Pacientes frecuentes (tratamientos en curso): tendrán varias consultas en el mes
            List<Pac> perrosYGatos = new ArrayList<>(pacientes.stream().filter(x -> !x.especie.equals("Conejo") && !x.especie.equals("Hamster")).toList());
            Collections.shuffle(perrosYGatos, rnd);
            for (int i = 0; i < 12 && i < perrosYGatos.size(); i++) perrosYGatos.get(i).frecuente = true;
        }

        void prepararHorarios() {
            enTx(null, () -> {
                for (Usuario u : personalConTurno()) {
                    Usuario gestionado = usuarioRepository.findById(u.getId()).orElseThrow();
                    if (!horarioRepository.findByUsuario(gestionado).isEmpty()) continue;
                    Map<DayOfWeek, LocalTime[]> m = turnos.get(u.getId());
                    if (m == null) continue;
                    for (Map.Entry<DayOfWeek, LocalTime[]> e : m.entrySet()) {
                        HorarioTrabajador h = new HorarioTrabajador();
                        h.setUsuario(gestionado);
                        h.setDiaSemana(e.getKey());
                        h.setHoraInicio(e.getValue()[0]);
                        h.setHoraFin(e.getValue()[1]);
                        h.setActivo(true);
                        h = horarioRepository.save(h);
                        registrar("HORARIO", h.getId());
                        contar("horarios", 1);
                    }
                }
                return null;
            });
        }

        // ═════════════════════════════ Un día ═════════════════════════════

        void simularDia(LocalDate d, int idx) {
            List<Evento> eventos = new ArrayList<>();
            diaActual = d;
            boolean domingo = d.getDayOfWeek() == DayOfWeek.SUNDAY;
            boolean sabado = d.getDayOfWeek() == DayOfWeek.SATURDAY;
            LocalTime abre = domingo ? LocalTime.of(9, 0) : apertura;
            LocalTime cierra = domingo ? LocalTime.of(13, 0) : cierre;

            // Compras: inventario inicial el primer día y reposiciones cada ~8 días
            if (idx == 0) {
                for (int p = 0; p < PROVEEDORES.length; p++) {
                    final int prov = p;
                    LocalDateTime t = en(d, abre.plusMinutes(2 + 5L * p));
                    eventos.add(new Evento(t, secuencia++, "compra inicial", () -> compra(prov, t, true)));
                }
            }
            if (esDiaDeReposicion(d, idx)) {
                for (int p = 0; p < PROVEEDORES.length; p++) {
                    final int prov = p;
                    LocalDateTime t = en(d, LocalTime.of(11, 20).plusMinutes(9L * p));
                    eventos.add(new Evento(t, secuencia++, "reposición", () -> compra(prov, t, false)));
                }
            }

            planificarCitas(d, idx, abre, cierra, domingo, sabado, eventos);

            // Ventas de mostrador
            int nVentas = (domingo ? entre(3, 5) : sabado ? entre(11, 15) : entre(5, 12)) * Math.max(1, factorVentas);
            List<Long[]> idsVenta = new ArrayList<>();
            for (int i = 0; i < nVentas; i++) {
                LocalDateTime t = en(d, horaVenta(abre, cierra, sabado));
                Long[] holder = new Long[1];
                idsVenta.add(holder);
                eventos.add(new Evento(t, secuencia++, "venta", () -> holder[0] = venta(t)));
                if ((idx == 9 || idx == 21) && i == 1) {
                    LocalDateTime tope = LocalDateTime.of(d, cierra.minusMinutes(4));
                    LocalDateTime ta0 = t.plusMinutes(entre(18, 45));
                    LocalDateTime ta = ta0.isAfter(tope) ? t.plusMinutes(2) : ta0;
                    String motivo = idx == 9 ? "Error en el cobro: se registró la presentación equivocada"
                            : "El cliente desistió de la compra";
                    eventos.add(new Evento(ta, secuencia++, "anulación", () -> anularVenta(holder[0], motivo, ta)));
                }
            }

            // Ajustes del mes (merma, rotura, conteo físico) y baja del lote vencido
            agregarAjustes(d, idx, eventos);

            eventos.sort(Comparator.comparing((Evento e) -> e.t).thenComparingInt(e -> e.orden));
            for (Evento e : eventos) {
                try {
                    e.accion.run();
                } catch (Exception ex) {
                    contar("operacionesOmitidas", 1);
                    log.debug("Historial demo: se omitió {} del {}: {}", e.que, e.t, ex.getMessage());
                }
            }
            asistencia(d);
        }

        boolean esDiaDeReposicion(LocalDate d, int idx) {
            for (int base : new int[]{8, 16, 24}) {
                LocalDate dia = inicio.plusDays(base);
                if (dia.getDayOfWeek() == DayOfWeek.SUNDAY) dia = dia.plusDays(1);
                if (dia.equals(d)) return true;
            }
            return false;
        }

        LocalTime horaVenta(LocalTime abre, LocalTime cierra, boolean sabado) {
            int desde = abre.toSecondOfDay() / 60 + 30;
            int hasta = cierra.toSecondOfDay() / 60 - 10;
            List<Integer> minutos = new ArrayList<>();
            for (int m = desde; m <= hasta; m += 5) minutos.add(m);
            Integer m = ponderado(minutos, x -> {
                int h = x / 60;
                if (sabado) return h < 14 ? 3.5 : 1.2;
                if (h >= 10 && h < 13) return 2.5;
                if (h >= 17 && h < 20) return 3;
                return 1.3;
            });
            return LocalTime.of(m / 60, m % 60);
        }

        // ─────────── Compras ───────────

        String codigoLote(LocalDateTime t) {
            char a = (char) ('A' + rnd.nextInt(26));
            char b = (char) ('A' + rnd.nextInt(12));
            return "" + a + String.format("%02d", t.getYear() % 100) + b + entre(100, 999);
        }

        LocalDate vencimiento(Prod p, LocalDate desde) {
            if (p.vencMin() <= 0) return null;
            LocalDate v = desde.plusMonths(entre(p.vencMin(), p.vencMax()));
            return v.withDayOfMonth(v.lengthOfMonth());
        }

        void compra(int prov, LocalDateTime t, boolean inicial) {
            Usuario quien = inicial && admin != null ? admin : almacen();
            enTx(quien, () -> {
                Compra c = new Compra();
                Proveedor ref = new Proveedor();
                ref.setId(proveedorId.get(prov));
                c.setProveedor(ref);
                int nro = facturaProveedor.merge(prov, entre(3, 40), Integer::sum);
                c.setNumeroFactura(String.format("F%03d-%08d", prov + 1, nro));
                List<DetalleCompra> detalles = new ArrayList<>();
                int lineaVencida = -1;
                for (Prod p : def.values()) {
                    if (p.proveedor() != prov || p.clase() == ClaseProducto.SERVICIO || p.objetivo() <= 0) continue;
                    Pres pc = p.pres().get(p.compraIdx());
                    int cantidad;
                    if (inicial) {
                        cantidad = p.objetivo();
                    } else {
                        if (!p.reponer()) continue;
                        Producto actual = productoRepository.findById(prodId.get(p.codigo())).orElseThrow();
                        int objetivoBase = p.objetivo() * pc.factor();
                        int stock = actual.getStock() == null ? 0 : actual.getStock();
                        if (stock >= objetivoBase * 0.45) continue;
                        cantidad = (int) Math.ceil((objetivoBase - stock) / (double) pc.factor());
                        if (cantidad <= 0) continue;
                    }
                    BigDecimal costo = new BigDecimal(p.costoCompra());
                    if (!inicial) {
                        costo = costo.multiply(BigDecimal.valueOf(entre(0.97, 1.05))).setScale(2, RoundingMode.HALF_UP);
                    }
                    LocalDate venc = vencimiento(p, t.toLocalDate());
                    if (inicial) {
                        for (String[] pv : LOTES_POR_VENCER) {
                            if (pv[0].equals(p.codigo())) venc = hoy.plusDays(Integer.parseInt(pv[1]));
                        }
                    }
                    detalles.add(detalle(p, p.compraIdx(), cantidad, costo, venc, t));
                    if (inicial && p.codigo().equals(PRODUCTO_LOTE_VENCIDO)) {
                        // Lote antiguo del proveedor: vence durante el mes (se dará de baja)
                        detalles.add(detalle(p, 0, 4, costo.divide(BigDecimal.valueOf(pc.factor()), 2, RoundingMode.HALF_UP),
                                hoy.plusMonths(6), t));
                        lineaVencida = detalles.size() - 1;
                    }
                }
                if (detalles.isEmpty()) return null;
                c.setDetalles(detalles);
                Compra g = compraService.registrarCompra(c);
                g.setFechaRegistro(t);
                for (DetalleCompra dc : g.getDetalles()) {
                    if (dc.getLote() != null) dc.getLote().setFechaIngreso(t);
                }
                if (lineaVencida >= 0) {
                    DetalleCompra dv = g.getDetalles().get(lineaVencida);
                    venceLote = inicio.plusDays(11);
                    dv.getLote().setFechaVencimiento(venceLote);
                    dv.setFechaVencimiento(venceLote);
                    loteQueVence = dv.getLote().getId();
                }
                fecharKardex("COMPRA", String.valueOf(g.getId()), t, null);
                registrar("COMPRA", g.getId());
                contar("compras", 1);
                contar("lotes", g.getDetalles().size());
                return null;
            });
        }

        DetalleCompra detalle(Prod p, int presIdx, int cantidad, BigDecimal costo, LocalDate venc, LocalDateTime t) {
            DetalleCompra d = new DetalleCompra();
            Producto ref = new Producto();
            ref.setId(prodId.get(p.codigo()));
            d.setProducto(ref);
            Presentacion pr = new Presentacion();
            pr.setId(presId.get(p.codigo())[presIdx]);
            d.setPresentacion(pr);
            d.setCantidadPresentacion(cantidad);
            d.setCostoPresentacion(costo);
            boolean conLote = p.clase().isLotePorDefecto() || venc != null;
            d.setNumeroLote(conLote ? codigoLote(t) : null);
            d.setFechaVencimiento(venc);
            return d;
        }

        // ─────────── Ventas ───────────

        int disponible(String codigo) {
            return loteRepository.findLotesVigentesFefo(prodId.get(codigo), LocalDate.now()).stream()
                    .mapToInt(Lote::getStockActual).sum();
        }

        Long venta(LocalDateTime t) {
            Usuario quien = cajero(t);
            return enTx(quien, () -> {
                List<Prod> candidatos = def.values().stream().filter(p -> p.popularidad() > 0).toList();
                int lineas = rnd.nextDouble() < 0.55 ? 1 : rnd.nextDouble() < 0.7 ? 2 : 3;
                Venta v = new Venta();
                List<DetalleVenta> lineasVenta = new ArrayList<>();
                Set<String> usados = new HashSet<>();
                boolean groomerHoy = enTurno(groomer, t);
                for (int i = 0; i < lineas * 3 && lineasVenta.size() < lineas; i++) {
                    Prod p = ponderado(candidatos, x -> x.popularidad());
                    if (p == null || !usados.add(p.codigo())) continue;
                    boolean servicio = p.clase() == ClaseProducto.SERVICIO;
                    if (servicio && !groomerHoy) continue;
                    Object[] linea = elegirLinea(p);
                    int presIdx = (Integer) linea[0];
                    BigDecimal cant = (BigDecimal) linea[1];
                    int base = cant.multiply(BigDecimal.valueOf(p.pres().get(presIdx).factor())).setScale(0, RoundingMode.HALF_UP).intValue();
                    if (!servicio && disponible(p.codigo()) < base) continue;
                    DetalleVenta dv = new DetalleVenta();
                    Producto ref = new Producto();
                    ref.setId(prodId.get(p.codigo()));
                    dv.setProducto(ref);
                    Presentacion pr = new Presentacion();
                    pr.setId(presId.get(p.codigo())[presIdx]);
                    dv.setPresentacion(pr);
                    dv.setCantidadPresentacion(cant);
                    lineasVenta.add(dv);
                }
                if (lineasVenta.isEmpty()) return null;
                v.setDetalles(lineasVenta); // también enlaza cada detalle con su venta
                if (rnd.nextDouble() < 0.45) {
                    Cli c = uno(clientes);
                    v.setClienteDni(c.dni());
                    v.setClienteNombre(c.nombre());
                }
                Venta g = ventaService.registrarVentaDirecta(v);
                g.setFecha(t);
                fecharKardex("VENTA", String.valueOf(g.getId()), t, null);
                registrar("VENTA", g.getId());
                contar("ventas", 1);
                ingresosVentas += g.getTotal();
                return g.getId();
            });
        }

        /** {índice de presentación, cantidad} según el tipo de producto. */
        Object[] elegirLinea(Prod p) {
            List<Pres> pr = p.pres();
            if (p.unidad() == UnidadBase.GRAMO) {
                if (rnd.nextDouble() < 0.86) {
                    String[] kilos = {"1", "1", "1", "1.5", "2", "2", "2", "2.5", "3", "3", "4", "5"};
                    return new Object[]{1, new BigDecimal(uno(kilos))};
                }
                return new Object[]{2, BigDecimal.ONE};
            }
            if (p.unidad() == UnidadBase.TABLETA && pr.size() == 3) {
                if (rnd.nextDouble() < 0.55) return new Object[]{1, BigDecimal.valueOf(entre(1, 2))};
                return new Object[]{0, BigDecimal.valueOf(entre(4, 14))};
            }
            if (p.unidad() == UnidadBase.TABLETA) {
                return new Object[]{0, BigDecimal.valueOf(p.codigo().equals("PZ-MED-006") ? entre(1, 3) : 1)};
            }
            if (p.unidad() == UnidadBase.PIPETA) {
                if (rnd.nextDouble() < 0.2) return new Object[]{1, BigDecimal.ONE};
                return new Object[]{0, BigDecimal.valueOf(entre(1, 2))};
            }
            if (p.codigo().equals("PZ-ALI-004")) {
                if (rnd.nextDouble() < 0.1) return new Object[]{1, BigDecimal.ONE};
                return new Object[]{0, BigDecimal.valueOf(entre(2, 8))};
            }
            int idx = 0;
            for (int i = 0; i < pr.size(); i++) {
                if (pr.get(i).vendible()) {
                    idx = i;
                    break;
                }
            }
            boolean dos = p.clase() != ClaseProducto.SERVICIO && rnd.nextDouble() < 0.12;
            return new Object[]{idx, BigDecimal.valueOf(dos ? 2 : 1)};
        }

        void anularVenta(Long ventaId, String motivo, LocalDateTime t) {
            if (ventaId == null) return;
            enTx(admin != null ? admin : almacen(), () -> {
                Venta v = ventaService.anularVenta(ventaId, motivo);
                v.setFechaAnulacion(t);
                fecharKardex("VENTA", String.valueOf(ventaId), t, InventarioService.ENTRADA);
                contar("ventasAnuladas", 1);
                ingresosVentas -= v.getTotal();
                return null;
            });
        }

        // ─────────── Ajustes y baja ───────────

        void agregarAjustes(LocalDate d, int idx, List<Evento> eventos) {
            int[][] dias = {{5}, {14}, {20}, {27}};
            String[][] datos = {
                    {"PZ-HIG-001", "SALIDA", "1", "ROTURA", "Frasco roto al reponer el anaquel", "16:40"},
                    {"PZ-ALI-001", "SALIDA", "350", "MERMA", "Merma por humedad en el saco abierto (granel)", "19:15"},
                    {"PZ-INS-002", "SALIDA", "3", "CONTEO_FISICO", "Diferencia en el conteo físico semanal", "19:30"},
                    {"PZ-ACC-001", "ENTRADA", "1", "CONTEO_FISICO", "Unidad encontrada en vitrina durante el conteo físico", "19:35"},
            };
            for (int i = 0; i < dias.length; i++) {
                LocalDate dia = inicio.plusDays(dias[i][0]);
                if (dia.getDayOfWeek() == DayOfWeek.SUNDAY) dia = dia.plusDays(1);
                if (!dia.equals(d)) continue;
                String[] a = datos[i];
                LocalDateTime t = en(d, LocalTime.parse(a[5]));
                eventos.add(new Evento(t, secuencia++, "ajuste", () -> ajuste(a, t)));
            }
            if (venceLote != null) {
                LocalDate diaBaja = venceLote.plusDays(1);
                if (diaBaja.getDayOfWeek() == DayOfWeek.SUNDAY) diaBaja = diaBaja.plusDays(1);
                if (diaBaja.equals(d) && loteQueVence != null) {
                    LocalDateTime t = en(d, LocalTime.of(11, 25));
                    eventos.add(new Evento(t, secuencia++, "baja por vencimiento", () -> baja(t)));
                }
            }
        }

        void ajuste(String[] a, LocalDateTime t) {
            enTx(almacen(), () -> {
                Long pid = prodId.get(a[0]);
                int cantidad = Integer.parseInt(a[2]);
                List<Lote> lotes = loteRepository.findLotesVigentesFefo(pid, LocalDate.now());
                Lote lote = lotes.stream().filter(l -> l.getStockActual() >= cantidad).findFirst().orElse(null);
                if (lote == null) return null;
                AjusteRequestDTO req = new AjusteRequestDTO();
                req.setProductoId(pid);
                req.setTipo(a[1]);
                req.setCantidad(cantidad);
                req.setLoteId(lote.getId());
                req.setMotivo(a[3]);
                req.setObservacion(a[4]);
                AjusteInventario aj = inventarioService.registrarAjuste(req);
                aj.setFecha(t);
                fecharKardex("AJUSTE", String.valueOf(aj.getId()), t, null);
                registrar("AJUSTE", aj.getId());
                contar("ajustes", 1);
                return null;
            });
        }

        void baja(LocalDateTime t) {
            enTx(almacen(), () -> {
                AjusteInventario aj = inventarioService.darDeBajaLote(loteQueVence, "Retirado del anaquel y separado para devolución al proveedor");
                aj.setFecha(t);
                fecharKardex("AJUSTE", String.valueOf(aj.getId()), t, null);
                registrar("AJUSTE", aj.getId());
                contar("ajustes", 1);
                contar("bajasPorVencimiento", 1);
                return null;
            });
        }

        // ─────────── Citas y consultas ───────────

        Caso elegirCaso(boolean domingo, int cirugiasHoy, boolean sabado) {
            Map<Tipo, Double> pesos = new EnumMap<>(Tipo.class);
            pesos.put(Tipo.GENERAL, 47.0);
            pesos.put(Tipo.VACUNA, 17.0);
            pesos.put(Tipo.DERMA, 12.0);
            pesos.put(Tipo.CONTROL, 11.0);
            int maxCirugias = domingo ? 0 : sabado ? 2 : 1;
            pesos.put(Tipo.CIRUGIA, cirugiasHoy < maxCirugias ? 9.0 : 0.0);
            Tipo tipo = ponderado(new ArrayList<>(pesos.keySet()), pesos::get);
            List<Caso> l = CASOS.stream().filter(c -> c.tipo() == tipo).toList();
            return ponderado(l, c -> c.peso());
        }

        long diasDesde(LocalDate desde) {
            return Math.abs(java.time.temporal.ChronoUnit.DAYS.between(desde, diaActual));
        }

        boolean especieCompatible(Caso c, Pac p) {
            return switch (c.especie()) {
                case "*" -> p.especie.equals("Perro") || p.especie.equals("Gato");
                case "Otro" -> p.especie.equals("Conejo") || p.especie.equals("Hamster");
                default -> c.especie().equals(p.especie);
            };
        }

        Pac elegirPaciente(Caso c, Set<String> ocupados) {
            List<Pac> l = pacientes.stream().filter(p -> especieCompatible(c, p) && !ocupados.contains(p.codigo)).filter(p -> {
                long meses = Period.between(p.nacimiento, hoy).toTotalMonths();
                if (c.motivoCita().startsWith("Primera vacuna")) return meses < 8;
                if (c.tipo() == Tipo.VACUNA) {
                    if (meses < 6) return false;
                    for (String v : c.vacunas().split(",")) if (p.vacunas.contains(v)) return false;
                }
                if (c.tipo() == Tipo.CIRUGIA && c.motivoCita().startsWith("Esterilización")) {
                    if (p.operado || meses < 6 || meses > 96) return false;
                    if (c.especie().equals("Perro") && !p.genero.equals("Hembra")) return false;
                }
                if (c.motivoCita().equals("Control post-operatorio")) return p.operado;
                if (c.motivoCita().equals("Control dermatológico")) return p.derma;
                return true;
            }).toList();
            // Reparte las visitas: los pacientes frecuentes vuelven más, pero nadie viene todos los días
            return ponderado(l, p -> {
                double w = (p.frecuente ? 3.0 : 1.0) / (1 + 0.7 * p.visitas);
                if (p.ultima != null && diasDesde(p.ultima) < (c.tipo() == Tipo.CONTROL ? 4 : 6)) w *= 0.1;
                return w;
            });
        }

        Vet elegirVet(Caso c, LocalDate d) {
            List<Vet> enTurno = vets.stream().filter(v -> turno(v.u(), d) != null).toList();
            if (enTurno.isEmpty()) return null;
            List<Vet> preferidos = switch (c.tipo()) {
                case CIRUGIA -> enTurno.stream().filter(v -> v.esp() == Tipo.CIRUGIA).toList();
                case DERMA -> enTurno.stream().filter(v -> v.esp() == Tipo.DERMA).toList();
                default -> enTurno.stream().filter(v -> v.esp() == Tipo.GENERAL).toList();
            };
            if (!preferidos.isEmpty() && rnd.nextDouble() < 0.8) return uno(preferidos);
            return uno(enTurno);
        }

        boolean libre(List<int[]> ocupado, int ini, int dur) {
            for (int[] o : ocupado) {
                if (ini < o[1] && o[0] < ini + dur) return false;
            }
            return true;
        }

        LocalTime elegirHora(Vet v, Caso c, LocalDate d, LocalTime abre, LocalTime cierra, List<int[]> ocupado, boolean sabado) {
            LocalTime[] t = turno(v.u(), d);
            int desde = Math.max(t[0].toSecondOfDay(), abre.toSecondOfDay()) / 60;
            // Termina al menos 15 min antes del cierre: queda tiempo para cobrar en caja
            int hasta = Math.min(t[1].toSecondOfDay(), cierra.toSecondOfDay()) / 60 - c.duracion() - 15;
            if (c.tipo() == Tipo.CIRUGIA) hasta = Math.min(hasta, 12 * 60);
            List<Integer> slots = new ArrayList<>();
            for (int m = desde; m <= hasta; m += 15) {
                if (libre(ocupado, m, c.duracion())) slots.add(m);
            }
            if (slots.isEmpty()) return null;
            Integer m = ponderado(slots, x -> {
                int h = x / 60;
                if (sabado) return h < 13 ? 4 : 1.2;
                if (h >= 9 && h < 12) return 3;
                if (h >= 16 && h < 19) return 2.6;
                if (h == 12 || h == 13) return 1;
                return 1.6;
            });
            return LocalTime.of(m / 60, m % 60);
        }

        String nuevoCodigoCita(LocalDate d, int[] correlativo) {
            if (correlativo[0] < 0) {
                String prefijo = "CIT-" + d.format(COD_FECHA) + "-";
                int max = 0;
                for (Cita c : citaRepository.findByCodigoCitaStartingWith(prefijo)) {
                    try {
                        max = Math.max(max, Integer.parseInt(c.getCodigoCita().substring(prefijo.length())));
                    } catch (NumberFormatException ignored) {
                        // código no estándar
                    }
                }
                correlativo[0] = max;
            }
            String codigo;
            do codigo = String.format("CIT-%s-%03d", d.format(COD_FECHA), ++correlativo[0]);
            while (citaRepository.existsById(codigo));
            return codigo;
        }

        void planificarCitas(LocalDate d, int idx, LocalTime abre, LocalTime cierra, boolean domingo, boolean sabado, List<Evento> eventos) {
            int n = domingo ? entre(3, 5) : sabado ? entre(14, 18)
                    : d.getDayOfWeek() == DayOfWeek.FRIDAY ? entre(10, 15) : entre(8, 14);
            Map<String, List<int[]>> ocupacion = new HashMap<>();
            for (Vet v : vets) {
                List<int[]> l = new ArrayList<>();
                for (Cita c : citaRepository.findByVeterinarioDniAndFecha(v.v().getDni(), d)) {
                    if (c.getEstado() == Cita.EstadoCita.CANCELADA || c.getHora() == null) continue;
                    int ini = c.getHora().toSecondOfDay() / 60;
                    l.add(new int[]{ini, ini + (c.getDuracionMinutos() != null ? c.getDuracionMinutos() : 30)});
                }
                ocupacion.put(v.v().getDni(), l);
            }
            Set<String> pacientesHoy = new HashSet<>();
            int[] correlativo = {-1};
            int cirugias = 0;
            int hechas = 0;
            List<Object[]> atendidas = new ArrayList<>();
            for (int intento = 0; intento < n * 4 && hechas < n; intento++) {
                Caso caso = elegirCaso(domingo, cirugias, sabado);
                if (caso == null) continue;
                Vet vet = elegirVet(caso, d);
                if (vet == null) return;
                Pac pac = elegirPaciente(caso, pacientesHoy);
                if (pac == null) continue;
                List<int[]> ocupado = ocupacion.get(vet.v().getDni());
                LocalTime hora = elegirHora(vet, caso, d, abre, cierra, ocupado, sabado);
                if (hora == null) continue;
                double q = rnd.nextDouble();
                Cita.EstadoCita estado = q < 0.07 ? Cita.EstadoCita.CANCELADA : q < 0.12 ? null : Cita.EstadoCita.REALIZADA;
                String codigo = guardarCitaPasada(d, hora, caso, pac, vet, estado == null ? Cita.EstadoCita.PENDIENTE : estado, correlativo);
                if (codigo == null) continue;
                int ini = hora.toSecondOfDay() / 60;
                ocupado.add(new int[]{ini, ini + caso.duracion()});
                pacientesHoy.add(pac.codigo);
                pac.visitas++;
                pac.ultima = d;
                if (caso.tipo() == Tipo.DERMA) pac.derma = true;
                hechas++;
                contar("citas", 1);
                if (estado == Cita.EstadoCita.CANCELADA) {
                    contar("citasCanceladas", 1);
                    continue;
                }
                if (estado == null) {
                    contar("citasNoAsistio", 1);
                    continue;
                }
                if (caso.tipo() == Tipo.CIRUGIA) {
                    cirugias++;
                    if (caso.motivoCita().startsWith("Esterilización")) pac.operado = true;
                }
                if (caso.tipo() == Tipo.VACUNA) pac.vacunas.addAll(Arrays.asList(caso.vacunas().split(",")));
                atendidas.add(new Object[]{codigo, hora, caso, pac, vet});
            }
            // Los dos últimos cobros del último día quedan pendientes en caja
            atendidas.sort(Comparator.comparing(o -> (LocalTime) o[1]));
            for (int i = 0; i < atendidas.size(); i++) {
                Object[] o = atendidas.get(i);
                boolean pendiente = d.equals(fin) && i >= atendidas.size() - 2;
                String codigoCita = (String) o[0];
                LocalTime hora = (LocalTime) o[1];
                Caso caso = (Caso) o[2];
                Pac pac = (Pac) o[3];
                Vet vet = (Vet) o[4];
                LocalDateTime tConsulta = en(d, hora.plusMinutes(entre(0, 4)));
                LocalDateTime tFin = tConsulta.plusMinutes(caso.duracion());
                LocalDateTime tPago = tFin.plusMinutes(entre(2, 9));
                Object[] cobro = new Object[2];
                eventos.add(new Evento(tConsulta, secuencia++, "consulta",
                        () -> atender(codigoCita, d, caso, pac, vet, tConsulta, tFin, cobro)));
                if (!pendiente) {
                    eventos.add(new Evento(tPago, secuencia++, "cobro", () -> pagar(cobro, tPago)));
                }
            }
        }

        String guardarCitaPasada(LocalDate d, LocalTime hora, Caso caso, Pac pac, Vet vet, Cita.EstadoCita estado, int[] correlativo) {
            return enTx(recep, () -> {
                // Mismas comprobaciones que CitaService (horario y traslape), sin la regla de "no en el pasado"
                LocalTime finCita = hora.plusMinutes(caso.duracion());
                if (hora.isBefore(apertura) || finCita.isAfter(cierre) || finCita.isBefore(hora)) return null;
                for (Cita c : citaRepository.findByVeterinarioDniAndFecha(vet.v().getDni(), d)) {
                    if (cruza(c, hora, finCita)) return null;
                }
                for (Cita c : citaRepository.findByPacienteCodigoPacienteAndFecha(pac.codigo, d)) {
                    if (cruza(c, hora, finCita)) return null;
                }
                Cita c = new Cita();
                c.setCodigoCita(nuevoCodigoCita(d, correlativo));
                c.setFecha(d);
                c.setHora(hora);
                c.setMotivo(caso.motivoCita());
                c.setDuracionMinutos(caso.duracion());
                c.setEstado(estado);
                c.setPaciente(pacienteRepository.findById(pac.codigo).orElseThrow());
                c.setVeterinario(veterinarioRepository.findById(vet.v().getDni()).orElseThrow());
                citaRepository.save(c);
                registrar("CITA", c.getCodigoCita());
                return c.getCodigoCita();
            });
        }

        boolean cruza(Cita c, LocalTime ini, LocalTime fin) {
            if (c.getEstado() == Cita.EstadoCita.CANCELADA || c.getHora() == null) return false;
            LocalTime cIni = c.getHora();
            LocalTime cFin = cIni.plusMinutes(c.getDuracionMinutos() != null ? c.getDuracionMinutos() : 30);
            return ini.isBefore(cFin) && cIni.isBefore(fin);
        }

        double redondear(double v, int decimales) {
            double f = Math.pow(10, decimales);
            return Math.round(v * f) / f;
        }

        void atender(String codigoCita, LocalDate d, Caso caso, Pac pac, Vet vet, LocalDateTime t, LocalDateTime tFin, Object[] cobro) {
            enTx(vet.u(), () -> {
                double peso = pac.peso * entre(0.97, 1.03);
                peso = pac.especie.equals("Hamster") ? redondear(peso, 3) : redondear(peso, 1);
                ConsultaDTO dto = new ConsultaDTO();
                dto.setFecha(d);
                dto.setCitaCodigo(codigoCita);
                dto.setMotivo(caso.motivo());
                dto.setPeso(peso);
                dto.setDiagnostico(caso.diagnostico());
                dto.setTratamiento(caso.tratamiento());
                dto.setObservaciones(caso.observaciones());
                dto.setEstadoIngreso(caso.estadoIngreso());
                dto.setEstadoSalida(caso.estadoSalida());
                dto.setRequiereInternamiento(false);
                ConsultaResponseDTO r = consultaService.registrarConsulta(dto);
                String codigo = r.getCodigoConsulta();
                Consulta c = consultaRepository.findById(codigo).orElseThrow();
                signosVitales(c, caso, pac);
                registrar("CONSULTA", codigo);
                contar("consultas", 1);
                contar("citasAtendidas", 1);

                List<Map<String, Object>> cargos = new ArrayList<>();
                if (caso.servicio() != null) {
                    Prod s = def.get(caso.servicio());
                    String desc = s.nombre();
                    double precio = Double.parseDouble(s.pres().get(0).precio());
                    if (caso.servicio().equals("PZ-SER-006") && pac.especie.equals("Gato")) precio = 220.0;
                    cargos.add(cargo(desc, precio, 1, caso.servicio()));
                }
                // Vacunas: se descuenta la dosis del lote (FEFO) y se registra la aplicación
                if (caso.vacunas() != null) {
                    for (String codVac : caso.vacunas().split(",")) {
                        Lote lote = loteRepository.findLotesVigentesFefo(prodId.get(codVac), LocalDate.now()).stream()
                                .findFirst().orElse(null);
                        if (lote == null) continue;
                        RegistroVacunaDTO rv = new RegistroVacunaDTO();
                        rv.setVacunaId(vacunaPorProducto.get(codVac));
                        rv.setPacienteCodigo(pac.codigo);
                        rv.setVeterinarioDni(vet.v().getDni());
                        rv.setFechaAplicacion(d);
                        rv.setLoteId(lote.getId());
                        boolean cachorro = caso.motivoCita().startsWith("Primera vacuna");
                        rv.setProximaDosis(cachorro ? d.plusDays(21) : d.plusYears(1));
                        rv.setObservaciones("Lote " + lote.getCodigoLote() + (cachorro ? " · 1.ª dosis del plan de cachorro" : " · refuerzo anual"));
                        RegistroVacunaResponseDTO reg = registroVacunaService.registrar(rv);
                        fecharKardex("VACUNA", String.valueOf(reg.getId()), t, null);
                        registrar("REG_VACUNA", reg.getId());
                        contar("vacunasAplicadas", 1);
                        Prod pv = def.get(codVac);
                        cargos.add(cargo(pv.nombre(), Double.parseDouble(pv.pres().get(0).precio()), 1, ""));
                    }
                }
                // Insumos y medicamentos usados en la consulta (CONSUMO_INTERNO al pagar el cobro)
                if (caso.insumos() != null) {
                    for (String par : caso.insumos().split(",")) {
                        String[] x = par.split(":");
                        int cant = Integer.parseInt(x[1]);
                        if (disponible(x[0]) < cant + 2) continue;
                        Prod pi = def.get(x[0]);
                        cargos.add(cargo(pi.nombre(), Double.parseDouble(pi.pres().get(0).precio()), cant, x[0]));
                    }
                }
                if (caso.receta() != null) {
                    RecetaMedica rec = new RecetaMedica();
                    List<Map<String, String>> meds = new ArrayList<>();
                    for (var m : caso.receta()) {
                        Map<String, String> mm = new LinkedHashMap<>();
                        mm.put("nombre", m.nombre());
                        mm.put("indicacion", m.indicacion());
                        mm.put("frecuencia", m.frecuencia());
                        mm.put("duracion", m.duracion());
                        meds.add(mm);
                    }
                    rec.setMedicamentos(json(meds));
                    rec.setIndicaciones(caso.indicaciones() != null ? caso.indicaciones() : "Volver a control si no hay mejoría.");
                    RecetaMedica g = recetaMedicaService.generarReceta(codigo, rec);
                    g.setFechaEmision(tFin.minusMinutes(entre(1, 4)));
                    registrar("RECETA", g.getId());
                    contar("recetas", 1);
                }
                if (cargos.isEmpty()) return null;
                double total = 0;
                for (Map<String, Object> cg : cargos) total += (Double) cg.get("subtotal");
                CobroConsulta cb = new CobroConsulta();
                cb.setFecha(tFin);
                cb.setTotal(redondear(total, 2));
                cb.setDetalleCargos(json(cargos));
                CobroConsulta g = cobroConsultaService.registrarCobro(codigo, cb);
                registrar("COBRO", g.getId());
                contar("cobros", 1);
                cobro[0] = g.getId();
                cobro[1] = codigo;
                return null;
            });
        }

        void signosVitales(Consulta c, Caso caso, Pac pac) {
            double temp;
            int fc;
            int fr;
            switch (pac.especie) {
                case "Gato" -> {
                    temp = caso.fiebre() ? entre(39.5, 40.2) : entre(38.1, 39.2);
                    fc = entre(150, 200);
                    fr = entre(20, 36);
                }
                case "Conejo" -> {
                    temp = entre(38.5, 39.6);
                    fc = entre(180, 250);
                    fr = entre(35, 60);
                }
                case "Hamster" -> {
                    temp = entre(36.5, 38.0);
                    fc = entre(300, 450);
                    fr = entre(60, 90);
                }
                default -> {
                    temp = caso.fiebre() ? entre(39.5, 40.3) : entre(38.0, 39.2);
                    fc = pac.peso < 10 ? entre(100, 140) : entre(70, 115);
                    fr = entre(16, 32);
                }
            }
            c.setTemperatura(redondear(temp, 1));
            c.setFrecuenciaCardiaca(fc);
            c.setFrecuenciaRespiratoria(fr);
            c.setTiempoLlenadoCapilar(caso.urgencia().equals("NORMAL") ? entre(1, 2) : 2);
            c.setNivelUrgencia(caso.urgencia());
            c.setSistemasAnormales(caso.sistemas());
        }

        Map<String, Object> cargo(String desc, double precio, int cantidad, String productoId) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("descripcion", desc);
            m.put("precio", precio);
            m.put("cantidad", cantidad);
            m.put("subtotal", redondear(precio * cantidad, 2));
            m.put("productoId", productoId);
            return m;
        }

        String json(Object o) {
            try {
                return objectMapper.writeValueAsString(o);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        void pagar(Object[] cobro, LocalDateTime t) {
            if (cobro[0] == null) return;
            Long id = (Long) cobro[0];
            String consulta = (String) cobro[1];
            enTx(cajero(t), () -> {
                CobroConsulta c = cobroConsultaService.pagarCobro(id);
                c.setFecha(t);
                fecharKardex("CONSULTA", consulta, t, null);
                contar("cobrosPagados", 1);
                ingresosConsultas += c.getTotal();
                return null;
            });
        }

        // ─────────── Asistencia ───────────

        void asistencia(LocalDate d) {
            int idx = (int) (d.toEpochDay() - inicio.toEpochDay());
            enTx(null, () -> {
                for (Usuario u : personalConTurno()) {
                    LocalTime[] h = turno(u, d);
                    if (h == null) continue;
                    Usuario g = usuarioRepository.findById(u.getId()).orElseThrow();
                    if (asistenciaRepository.findByUsuarioAndFecha(g, d).isPresent()) continue;
                    boolean tarde = tardanzas.contains(u.getId() + "|" + idx);
                    LocalTime entrada = tarde ? h[0].plusMinutes(entre(11, 27)) : h[0].minusMinutes(entre(1, 12));
                    LocalTime salida = h[1].plusMinutes(entre(-2, 18));
                    AsistenciaDiaria a = new AsistenciaDiaria();
                    a.setUsuario(g);
                    a.setFecha(d);
                    a.setHoraApertura(en(d, entrada));
                    a.setHoraCierre(en(d, salida));
                    a.setEstado(AsistenciaDiaria.EstadoAsistencia.CERRADO);
                    a = asistenciaRepository.save(a);
                    registrar("ASISTENCIA", a.getId());
                    contar("asistencias", 1);
                }
                return null;
            });
        }

        // ─────────── Citas programadas (próximos 7 días) ───────────

        void citasFuturas() {
            LocalDateTime ahora = LocalDateTime.now(CitaService.ZONA_CLINICA);
            for (int k = 0; k <= 7; k++) {
                LocalDate d = hoy.plusDays(k);
                diaActual = d;
                boolean domingo = d.getDayOfWeek() == DayOfWeek.SUNDAY;
                LocalTime abre = domingo ? LocalTime.of(9, 0) : apertura;
                LocalTime cierra = domingo ? LocalTime.of(13, 0) : cierre;
                int n = domingo ? entre(1, 2) : k <= 2 ? entre(5, 8) : entre(2, 5);
                Set<String> pacs = new HashSet<>();
                for (int i = 0, hechas = 0; i < n * 4 && hechas < n; i++) {
                    Caso caso = elegirCaso(domingo, 1, false);
                    if (caso == null || caso.tipo() == Tipo.CONTROL) continue;
                    Vet vet = elegirVet(caso, d);
                    if (vet == null) break;
                    Pac pac = elegirPaciente(caso, pacs);
                    if (pac == null) continue;
                    LocalTime hora = elegirHora(vet, caso, d, abre, cierra, List.of(), d.getDayOfWeek() == DayOfWeek.SATURDAY);
                    if (hora == null || !LocalDateTime.of(d, hora).isAfter(ahora.plusMinutes(60))) continue;
                    try {
                        String codigo = enTx(recep, () -> {
                            CitaDTO dto = new CitaDTO();
                            dto.setFecha(d);
                            dto.setHora(hora);
                            dto.setMotivo(caso.motivoCita());
                            dto.setPacienteCodigo(pac.codigo);
                            dto.setVeterinarioDni(vet.v().getDni());
                            dto.setDuracionMinutos(caso.duracion());
                            return citaService.registrarCita(dto).getCodigoCita();
                        });
                        registrar("CITA", codigo);
                        pacs.add(pac.codigo);
                        hechas++;
                        contar("citas", 1);
                        contar("citasProgramadas", 1);
                    } catch (Exception e) {
                        // horario ocupado: se prueba con otro
                    }
                }
            }
        }
    }

    // ═════════════════════════════ Eliminación ═════════════════════════════

    /**
     * Lista literal de ids numéricos registrados para un tipo, p. ej. "(4,5,6)". Se arma en Java (y no con
     * CAST(ref AS BIGINT) en una subconsulta) porque SQL Server puede evaluar el CAST antes del filtro por tipo
     * y fallar con las referencias no numéricas (DNI, códigos). Vacía → "(-1)", que no coincide con ningún id
     * y mantiene correcto el NOT IN.
     */
    private String ids(String tipo) {
        List<String> refsTipo = new JdbcTemplate(dataSource)
                .queryForList("SELECT ref FROM " + TABLA + " WHERE tipo = ?", String.class, tipo);
        StringBuilder sb = new StringBuilder("(");
        for (String r : refsTipo) {
            long id = Long.parseLong(r.trim()); // solo se registran ids numéricos para estos tipos
            if (sb.length() > 1) sb.append(',');
            sb.append(id);
        }
        return sb.length() > 1 ? sb.append(')').toString() : "(-1)";
    }

    private static String refs(String tipo) {
        return "(SELECT ref FROM " + TABLA + " WHERE tipo = '" + tipo + "')";
    }

    /**
     * Borra exactamente lo que generó el historial de demostración (según la tabla de registro).
     * Se niega (ConflictoException) si después de generarlo se registraron datos reales que dependen de lo generado
     * (por ejemplo una venta real de un producto de demostración).
     */
    public Map<String, Object> eliminar() {
        if (!enCurso.compareAndSet(false, true)) {
            throw new ConflictoException("Ya hay una generación o eliminación del historial de demostración en curso");
        }
        try {
            if (!existeTablaRegistro()) {
                throw new NoSuchElementException("No hay historial de demostración registrado en esta base de datos");
            }
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            Long filas = jdbc.queryForObject("SELECT COUNT(*) FROM " + TABLA, Long.class);
            if (filas == null || filas == 0) {
                throw new NoSuchElementException("No hay historial de demostración registrado en esta base de datos");
            }

            String P = ids("PRODUCTO"), V = ids("VENTA"), C = ids("COMPRA"), A = ids("AJUSTE"), U = ids("USUARIO");
            String PAC = refs("PACIENTE"), VET = refs("VETERINARIO"), CITA = refs("CITA"), CON = refs("CONSULTA");
            String[][] chequeos = {
                    {"ventas que no son de la demostración usan productos de demostración",
                            "SELECT COUNT(*) FROM detalle_ventas WHERE producto_id IN " + P + " AND venta_id NOT IN " + V},
                    {"compras que no son de la demostración usan productos de demostración",
                            "SELECT COUNT(*) FROM detalle_compras WHERE producto_id IN " + P + " AND compra_id NOT IN " + C},
                    {"ajustes de inventario posteriores sobre productos de demostración",
                            "SELECT COUNT(*) FROM ajustes_inventario WHERE producto_id IN " + P + " AND id NOT IN " + A},
                    {"citas posteriores de mascotas o veterinarios de demostración",
                            "SELECT COUNT(*) FROM citas WHERE (paciente_codigo IN " + PAC + " OR veterinario_dni IN " + VET
                                    + ") AND codigo_cita NOT IN " + CITA},
                    {"consultas posteriores de mascotas o veterinarios de demostración",
                            "SELECT COUNT(*) FROM consultas WHERE (paciente_codigo IN " + PAC + " OR veterinario_dni IN " + VET
                                    + " OR cita_codigo IN " + CITA + ") AND codigo_consulta NOT IN " + CON},
                    {"vacunas aplicadas posteriores a mascotas de demostración",
                            "SELECT COUNT(*) FROM registro_vacunas WHERE (paciente_codigo IN " + PAC + " OR veterinario_dni IN " + VET
                                    + " OR vacuna_id IN " + ids("VACUNA") + ") AND id NOT IN " + ids("REG_VACUNA")},
                    {"mascotas nuevas registradas a clientes de demostración",
                            "SELECT COUNT(*) FROM pacientes WHERE cliente_dni IN " + refs("CLIENTE") + " AND codigo_paciente NOT IN " + PAC},
                    {"movimientos de kardex de otros productos hechos por el personal de demostración",
                            "SELECT COUNT(*) FROM kardex WHERE usuario_id IN " + U + " AND producto_id NOT IN " + P},
                    {"compras de otros proveedores o registradas por el personal de demostración",
                            "SELECT COUNT(*) FROM compras WHERE (usuario_id IN " + U + " OR proveedor_id IN " + ids("PROVEEDOR")
                                    + ") AND id NOT IN " + C},
                    {"asistencias u horarios posteriores del personal de demostración",
                            "SELECT (SELECT COUNT(*) FROM asistencia_diaria WHERE usuario_id IN " + U + " AND id NOT IN " + ids("ASISTENCIA")
                                    + ") + (SELECT COUNT(*) FROM horario_trabajador WHERE usuario_id IN " + U + " AND id NOT IN "
                                    + ids("HORARIO") + ")"},
                    {"productos nuevos en categorías creadas por la demostración",
                            "SELECT COUNT(*) FROM productos WHERE categoria_id IN " + ids("CATEGORIA") + " AND id NOT IN " + P},
                    {"veterinarios nuevos con especialidades creadas por la demostración",
                            "SELECT COUNT(*) FROM veterinarios WHERE especialidad_id IN " + ids("ESPECIALIDAD") + " AND dni NOT IN " + VET},
                    {"mascotas nuevas con razas creadas por la demostración",
                            "SELECT COUNT(*) FROM pacientes WHERE raza_id IN " + ids("RAZA") + " AND codigo_paciente NOT IN " + PAC},
            };
            List<String> problemas = new ArrayList<>();
            for (String[] ch : chequeos) {
                Long n = jdbc.queryForObject(ch[1], Long.class);
                if (n != null && n > 0) problemas.add(n + " " + ch[0]);
            }
            // Kardex de productos de demostración que no viene de un documento de la demostración
            Long kardexTotal = jdbc.queryForObject("SELECT COUNT(*) FROM kardex WHERE producto_id IN " + P, Long.class);
            Long kardexDemo = jdbc.queryForObject("SELECT COUNT(*) FROM kardex WHERE producto_id IN " + P + " AND ("
                    + "(documento_tipo = 'VENTA' AND documento_id IN " + refs("VENTA") + ") OR "
                    + "(documento_tipo = 'COMPRA' AND documento_id IN " + refs("COMPRA") + ") OR "
                    + "(documento_tipo = 'AJUSTE' AND documento_id IN " + refs("AJUSTE") + ") OR "
                    + "(documento_tipo = 'CONSULTA' AND documento_id IN " + CON + ") OR "
                    + "(documento_tipo = 'VACUNA' AND documento_id IN " + refs("REG_VACUNA") + "))", Long.class);
            long ajenos = (kardexTotal == null ? 0 : kardexTotal) - (kardexDemo == null ? 0 : kardexDemo);
            if (ajenos > 0) problemas.add(ajenos + " movimientos de kardex reales sobre productos de demostración");
            if (!problemas.isEmpty()) {
                throw new ConflictoException("No se eliminó nada: después de generar la demostración se registraron datos reales "
                        + "que dependen de ella (" + String.join("; ", problemas) + "). Restaure la copia de seguridad.");
            }

            String[][] borrados = {
                    {"detalleVentas", "DELETE FROM detalle_ventas WHERE venta_id IN " + V},
                    {"ventas", "DELETE FROM ventas WHERE id IN " + V},
                    {"movimientosKardex", "DELETE FROM kardex WHERE producto_id IN " + P},
                    {"ajustes", "DELETE FROM ajustes_inventario WHERE producto_id IN " + P},
                    {"detalleCompras", "DELETE FROM detalle_compras WHERE compra_id IN " + C},
                    {"compras", "DELETE FROM compras WHERE id IN " + C},
                    {"lotes", "DELETE FROM lotes WHERE producto_id IN " + P},
                    {"presentaciones", "DELETE FROM presentaciones WHERE producto_id IN " + P},
                    {"cobros", "DELETE FROM cobro_consulta WHERE consulta_codigo IN " + CON},
                    {"recetas", "DELETE FROM recetas_medicas WHERE consulta_codigo IN " + CON},
                    {"vacunasAplicadas", "DELETE FROM registro_vacunas WHERE id IN " + ids("REG_VACUNA")},
                    {"consultas", "DELETE FROM consultas WHERE codigo_consulta IN " + CON},
                    {"citas", "DELETE FROM citas WHERE codigo_cita IN " + CITA},
                    {"productos", "DELETE FROM productos WHERE id IN " + P},
                    {"pacientes", "DELETE FROM pacientes WHERE codigo_paciente IN " + PAC},
                    {"telefonos", "DELETE FROM telefonos_cliente WHERE cliente_dni IN " + refs("CLIENTE")},
                    {"clientes", "DELETE FROM clientes WHERE dni IN " + refs("CLIENTE")},
                    {"vacunaEspecie", "DELETE FROM vacuna_especie WHERE vacuna_id IN " + ids("VACUNA")},
                    {"vacunas", "DELETE FROM vacunas WHERE id IN " + ids("VACUNA")},
                    {"asistencias", "DELETE FROM asistencia_diaria WHERE id IN " + ids("ASISTENCIA")},
                    {"horarios", "DELETE FROM horario_trabajador WHERE id IN " + ids("HORARIO")},
                    {"veterinarios", "DELETE FROM veterinarios WHERE dni IN " + VET},
                    {"tokens", "DELETE FROM password_reset_tokens WHERE usuario_id IN " + U},
                    {"usuarios", "DELETE FROM usuarios WHERE id IN " + U},
                    {"proveedores", "DELETE FROM proveedores WHERE id IN " + ids("PROVEEDOR")},
                    {"categorias", "DELETE FROM categorias WHERE id IN " + ids("CATEGORIA")},
                    {"especialidades", "DELETE FROM especialidades WHERE id IN " + ids("ESPECIALIDAD")},
                    {"razas", "DELETE FROM razas WHERE id IN " + ids("RAZA")},
                    {"registro", "DELETE FROM " + TABLA},
            };
            Map<String, Object> resumen = new LinkedHashMap<>();
            new TransactionTemplate(txManager).executeWithoutResult(st -> {
                for (String[] b : borrados) {
                    int n = jdbc.update(b[1]);
                    if (!b[0].equals("detalleVentas") && !b[0].equals("detalleCompras") && !b[0].equals("tokens")
                            && !b[0].equals("vacunaEspecie") && !b[0].equals("registro")) {
                        resumen.put(b[0], n);
                    }
                }
            });
            log.info("Historial de demostración eliminado: {}", resumen);
            return resumen;
        } finally {
            enCurso.set(false);
        }
    }
}
