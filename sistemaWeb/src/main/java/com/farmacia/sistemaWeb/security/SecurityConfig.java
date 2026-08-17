package com.farmacia.sistemaWeb.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * SecurityConfig — Configuración centralizada de seguridad HTTP para PetyZoos.
 *
 * <p>Esta clase define la cadena de filtros de seguridad ({@link SecurityFilterChain}),
 * el proveedor de autenticación basado en base de datos ({@link DaoAuthenticationProvider}),
 * la política de CORS y los encabezados de seguridad HTTP.</p>
 *
 * <p>Cambios clave respecto a la versión anterior (Java 21 / Spring Security 6):</p>
 * <ul>
 *   <li>Se reemplaza {@code @Autowired} por inyección por constructor (best practice en Spring).</li>
 *   <li>Se añade {@code .cors()} explícito con {@link CorsConfigurationSource}, requerido en Spring Security 6.</li>
 *   <li>Se usa {@code AbstractHttpConfigurer::disable} para deshabilitar CSRF con sintaxis de referencia de método.</li>
 *   <li>Se añaden reglas RBAC para los módulos de inventario unificados: proveedores, compras, kardex, lotes y salidas.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Habilita @PreAuthorize y @PostAuthorize a nivel de método.
public class SecurityConfig {

    // Inyección por constructor — elimina @Autowired de campo (más testeable y explícito).
    private final JwtEntryPoint      jwtEntryPoint;
    private final JwtFilter          jwtFilter;
    private final RateLimitFilter    rateLimitFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtEntryPoint jwtEntryPoint,
                          JwtFilter jwtFilter,
                          RateLimitFilter rateLimitFilter,
                          UserDetailsService userDetailsService) {
        this.jwtEntryPoint       = jwtEntryPoint;
        this.jwtFilter           = jwtFilter;
        this.rateLimitFilter     = rateLimitFilter;
        this.userDetailsService  = userDetailsService;
    }

    // ─────────────────────────────────────────────────────────
    // BEANS DE AUTENTICACIÓN
    // ─────────────────────────────────────────────────────────

    /**
     * El sistema usa BCrypt con strength 12 para el hashing de contraseñas.
     * Strength 12 ofrece mayor seguridad que el default (10) con un costo de cómputo aceptable.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * El sistema expone el {@link AuthenticationManager} para que el {@code AuthController}
     * pueda verificar credenciales sin depender de un filtro de formulario.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * El sistema configura el proveedor DAO para consultar usuarios de la BD
     * y verificar sus contraseñas con BCrypt.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * El sistema maneja los errores HTTP 403 (acceso denegado) devolviendo JSON estructurado
     * en lugar de la página HTML por defecto de Spring Security.
     *
     * <p>Sin este handler, cuando un usuario autenticado accede a un endpoint sin el rol
     * requerido, Spring retorna HTML 403 que el {@code ErrorInterceptor} de Angular
     * no puede parsear. El interceptor lo interpreta como un error de sesión y expulsa
     * al usuario al login — comportamiento incorrecto.</p>
     *
     * <p>Con este handler, Angular recibe JSON con {@code status: 403} y puede mostrar
     * el mensaje de "Sin permisos" sin cerrar la sesión.</p>
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        ObjectMapper mapper = new ObjectMapper();
        return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            Map<String, Object> cuerpoError = Map.of(
                "timestamp", Instant.now().toString(),
                "status",    HttpServletResponse.SC_FORBIDDEN,
                "error",     "Acceso denegado",
                "mensaje",   "No tienes permisos para realizar esta acción.",
                "path",      request.getRequestURI()
            );

            mapper.writeValue(response.getOutputStream(), cuerpoError);
        };
    }

    // ─────────────────────────────────────────────────────────
    // CADENA PRINCIPAL DE FILTROS
    // ─────────────────────────────────────────────────────────

    /**
     * El sistema define la cadena de filtros de seguridad HTTP con todas las reglas RBAC.
     *
     * <p>Los tres roles son: {@code ADMIN}, {@code VETERINARIO} y {@code RECEPCIONISTA}.
     * Spring Security espera que los roles se almacenen con el prefijo {@code ROLE_}
     * en el token JWT (ej: {@code "ROLE_ADMIN"}) para que {@code hasRole("ADMIN")} funcione.</p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF deshabilitado: el sistema usa JWT stateless, no cookies de sesión.
            .csrf(AbstractHttpConfigurer::disable)

            // CORS habilitado explícitamente con el bean de configuración definido más abajo.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── ENCABEZADOS DE SEGURIDAD HTTP ──
            .headers(headers -> headers
                // Previene clickjacking: el sistema no permite ser embebido en iframes ajenos.
                .frameOptions(frame -> frame.deny())
                // Previene MIME sniffing en navegadores.
                .contentTypeOptions(content -> {})
                // Activa la protección XSS del navegador en modo bloqueo activo.
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                // Política de Seguridad de Contenido (CSP): restringe fuentes de recursos permitidas.
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self'; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com; " +
                    "img-src 'self' data: blob: https://cdn-icons-png.flaticon.com;"))
                // HSTS: el sistema fuerza HTTPS durante 1 año en producción.
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31_536_000)))

            // El sistema dirige los errores 401 (no autenticado) al JwtEntryPoint (JSON).
            // El sistema dirige los errores 403 (sin permisos) al accessDeniedHandler (JSON).
            // Sin estos handlers, Spring devuelve HTML que el ErrorInterceptor de Angular no puede
            // procesar, haciendo que el frontend interprete el 403 como sesión inválida.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtEntryPoint)
                .accessDeniedHandler(accessDeniedHandler()))

            // El sistema es stateless: nunca crea ni utiliza sesiones HTTP.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ═══════════════════════════════════════════════════════
            // REGLAS DE AUTORIZACIÓN POR ROL (RBAC)
            // ═══════════════════════════════════════════════════════
            .authorizeHttpRequests(auth -> auth

                // ── RUTAS PÚBLICAS (sin token) ──
                .requestMatchers("/api/health", "/api/auth/login", "/api/auth/forgot-password", "/api/auth/reset-password", "/api/auth/registro", "/api/usuarios/admin", "/error").permitAll()
                .requestMatchers("/api/veterinarios/archivos/**").permitAll()
                .requestMatchers("/api/usuarios/foto/img/**").permitAll()
                .requestMatchers("/api/pacientes/foto/img/**").permitAll()

                // ── GESTIÓN DE USUARIOS ──
                .requestMatchers("/api/usuarios/vendedor", "/api/usuarios/crear").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,    "/api/usuarios").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/usuarios/*/desbloquear").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/usuarios/*/estado").hasRole("ADMIN")
                // Perfil propio: cualquier usuario autenticado gestiona su propia imagen y contraseña.
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/*/imagen").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/usuarios/*/imagen").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/*/password").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/*").authenticated()

                // ── VETERINARIOS ──
                .requestMatchers("/api/veterinarios/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                // ── PACIENTES ──
                .requestMatchers("/api/pacientes/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                // ── CONSULTAS / DIAGNÓSTICOS ──
                .requestMatchers("/api/consultas/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                // ── HISTORIAL CLÍNICO ──
                .requestMatchers("/api/historial-clinico/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                // ── ESTUPEFACIENTES (solo ADMIN y VETERINARIO pueden gestionar medicamentos controlados) ──
                .requestMatchers("/api/estupefacientes/**")
                    .hasAnyRole("ADMIN", "VETERINARIO")

                // ── ESPECIES Y RAZAS (lectura para todos, escritura solo ADMIN) ──
                .requestMatchers(HttpMethod.GET, "/api/especies", "/api/especies/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")
                .requestMatchers(HttpMethod.POST, "/api/especies", "/api/especies/**").hasRole("ADMIN")

                // ── ESPECIALIDADES (configuración del sistema, solo ADMIN) ──
                .requestMatchers("/api/especialidades/**").hasRole("ADMIN")

                // ── CITAS ──
                .requestMatchers("/api/citas/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                // ── CLIENTES ──
                .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasRole("ADMIN")
                .requestMatchers("/api/clientes/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                // ── PRODUCTOS (lectura para todos, escritura restringida a ADMIN) ──
                .requestMatchers(HttpMethod.GET,    "/api/productos", "/api/productos/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")
                .requestMatchers(HttpMethod.POST,   "/api/productos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/productos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")

                // ── CATEGORÍAS (lectura para todos, escritura restringida a ADMIN) ──
                .requestMatchers(HttpMethod.GET,    "/api/categorias", "/api/categorias/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")
                .requestMatchers(HttpMethod.POST,   "/api/categorias").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole("ADMIN")

                // ── VENTAS ──
                .requestMatchers("/api/ventas/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA")

                // ── MÓDULOS DE INVENTARIO UNIFICADOS ──

                // Proveedores: lectura para ADMIN/RECEPCIONISTA, escritura solo ADMIN.
                .requestMatchers(HttpMethod.GET, "/api/proveedores", "/api/proveedores/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA")
                .requestMatchers("/api/proveedores", "/api/proveedores/**").hasRole("ADMIN")

                // Compras: registrar y listar para ADMIN y RECEPCIONISTA.
                .requestMatchers("/api/compras", "/api/compras/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA")

                // Salidas / POS: cualquier rol autenticado puede registrar salidas de inventario.
                .requestMatchers("/api/salidas", "/api/salidas/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                // Kardex: solo lectura del historial de movimientos, para ADMIN y RECEPCIONISTA.
                .requestMatchers(HttpMethod.GET, "/api/kardex", "/api/kardex/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA")

                // Lotes: lectura para ADMIN/RECEPCIONISTA, gestión completa solo para ADMIN.
                .requestMatchers(HttpMethod.GET, "/api/lotes", "/api/lotes/**")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA")
                .requestMatchers("/api/lotes", "/api/lotes/**").hasRole("ADMIN")

                // ── REPORTES (exclusivo ADMIN) ──
                .requestMatchers("/api/reportes/**").hasRole("ADMIN")

                // ── TODO LO DEMÁS requiere autenticación válida ──
                .anyRequest().authenticated())

            .authenticationProvider(authenticationProvider())
            // El RateLimitFilter rechaza IPs bloqueadas antes de que el JWT sea validado.
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            // El JwtFilter valida el token y establece la autenticación antes de evaluar reglas de acceso.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ─────────────────────────────────────────────────────────
    // CONFIGURACIÓN CORS
    // ─────────────────────────────────────────────────────────

    /**
     * El sistema configura CORS para permitir peticiones solo desde los orígenes
     * definidos en la variable de entorno {@code CORS_ORIGINS}.
     * Soporte para múltiples dominios separados por coma (ej: {@code http://a.com,http://b.com}).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")).stream()
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Solo se permiten headers necesarios — sin wildcard (*) por seguridad.
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        // El frontend Angular necesita leer el header Authorization de las respuestas.
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3_600L); // El navegador cachea la respuesta preflight por 1 hora.

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
