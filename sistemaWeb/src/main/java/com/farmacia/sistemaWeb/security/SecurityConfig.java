package com.farmacia.sistemaWeb.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtEntryPoint jwtEntryPoint;

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt con strength 12 (mas seguro que el default 10)
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // === CSRF deshabilitado porque usamos JWT (stateless) ===
                .csrf(csrf -> csrf.disable())

                // === HEADERS DE SEGURIDAD ===
                .headers(headers -> headers
                        // Prevenir clickjacking (iframes maliciosos)
                        .frameOptions(frame -> frame.deny())
                        // Prevenir MIME sniffing
                        .contentTypeOptions(content -> {
                        })
                        // Proteccion XSS del navegador
                        .xssProtection(
                                xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        // Politica de seguridad de contenido
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data: blob:;"))
                        // Prevenir envio de referrer a sitios externos
                        .referrerPolicy(referrer -> {
                        })
                        // HSTS para forzar HTTPS en produccion
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)))

                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // === RUTAS PUBLICAS ===
                        .requestMatchers("/api/auth/login", "/api/usuarios/admin").permitAll()
                        .requestMatchers("/api/veterinarios/archivos/**").permitAll()

                        // === USUARIOS (gestion) ===
                        .requestMatchers("/api/usuarios/vendedor").hasRole("ADMIN")
                        .requestMatchers("/api/usuarios/crear").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")
                        // Perfil propio (imagen y password) - cualquier usuario autenticado
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/*/imagen").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/*/imagen").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/*/password").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/*/desbloquear").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/*/estado").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/*").authenticated()

                        // === VETERINARIOS ===
                        .requestMatchers("/api/veterinarios/**").hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                        // === PACIENTES ===
                        .requestMatchers("/api/pacientes/**").hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                        // === CONSULTAS ===
                        .requestMatchers("/api/consultas/**").hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                        // === CLIENTES ===
                        .requestMatchers(HttpMethod.POST, "/api/clientes")
                        .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/**")
                        .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/clientes/**")
                        .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                        // === PRODUCTOS ===
                        .requestMatchers(HttpMethod.POST, "/api/productos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/productos/**")
                        .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                        // === CATEGORIAS ===
                        .requestMatchers(HttpMethod.POST, "/api/categorias").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/categorias/**")
                        .hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                        // === VENTAS ===
                        .requestMatchers("/api/ventas/**").hasAnyRole("ADMIN", "RECEPCIONISTA")

                        // === CITAS ===
                        .requestMatchers("/api/citas/**").hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                        // === REPORTES ===
                        .requestMatchers("/api/reportes/**").hasRole("ADMIN")

                        // === TODO LO DEMAS requiere autenticacion ===
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Origenes permitidos desde variable de entorno (CORS_ORIGINS)
        List<String> origins = List.of(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Solo headers necesarios (no wildcard)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight 1 hora
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(
                new CorsFilter(corsConfigurationSource()));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
