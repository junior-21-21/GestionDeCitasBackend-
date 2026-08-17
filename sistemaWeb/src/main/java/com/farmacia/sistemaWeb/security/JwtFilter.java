package com.farmacia.sistemaWeb.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtFilter — Filtro de validación del token JWT por cada petición HTTP entrante.
 *
 * <p>El filtro intercepta cada request antes de que llegue a los controladores.
 * Extrae el token del header {@code Authorization}, lo valida mediante {@link JwtProvider}
 * y establece el contexto de seguridad de Spring con el usuario y sus roles.</p>
 *
 * <h3>Contrato de roles (fuente única de verdad):</h3>
 * <ul>
 *   <li>La DB almacena: {@code ADMIN}, {@code VETERINARIO}, {@code RECEPCIONISTA}</li>
 *   <li>El JWT claim {@code "roles"} contiene: {@code ["ADMIN"]} — SIN prefijo {@code ROLE_}</li>
 *   <li>Este filtro añade el prefijo al construir las authorities:
 *       {@code SimpleGrantedAuthority("ROLE_ADMIN")}</li>
 *   <li>Spring Security evalúa {@code hasRole("ADMIN")} como {@code hasAuthority("ROLE_ADMIN")} ✅</li>
 * </ul>
 *
 * <p>Este contrato garantiza que {@code hasRole("ADMIN")} en el SecurityFilterChain
 * y {@code @PreAuthorize("hasRole('ADMIN')")} en los controladores funcionan
 * correctamente sin doble prefijado ({@code ROLE_ROLE_ADMIN}).</p>
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    // El proveedor centralizado de JWT — única fuente de verdad para validación y extracción.
    @Autowired
    private JwtProvider jwtProvider;

    /**
     * El sistema ejecuta este método una sola vez por cada petición HTTP entrante.
     *
     * <p>Flujo de ejecución:</p>
     * <ol>
     *   <li>Extrae el token del header {@code Authorization: Bearer <token>}.</li>
     *   <li>Valida la firma y la expiración del token con {@link JwtProvider#validarToken}.</li>
     *   <li>Extrae el email (subject) y los roles del token.</li>
     *   <li>Añade el prefijo {@code ROLE_} a cada rol para cumplir el contrato de Spring Security.</li>
     *   <li>Construye un {@link UsernamePasswordAuthenticationToken} con las authorities.</li>
     *   <li>Almacena la autenticación en el {@link SecurityContextHolder}.</li>
     * </ol>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Paso 1: Extraer el token del header de la petición entrante.
        String token = extraerToken(request);

        // Paso 2: Solo continúa si el token existe y es criptográficamente válido.
        if (StringUtils.hasText(token) && jwtProvider.validarToken(token)) {

            // Paso 3: Extraer el email del usuario desde el claim "sub" del token.
            String email = jwtProvider.getEmailFromToken(token);

            // Paso 4: Construir las GrantedAuthority añadiendo el prefijo ROLE_.
            //
            // El JWT almacena roles SIN prefijo (ej: ["ADMIN", "VETERINARIO"]).
            // Spring Security requiere el prefijo ROLE_ para que hasRole() funcione.
            // Añadirlo aquí —y solo aquí— evita el doble prefijado ROLE_ROLE_ADMIN.
            List<SimpleGrantedAuthority> autoridades = jwtProvider.getRolesFromToken(token)
                    .stream()
                    .map(rol -> {
                        // Normalizar: si el rol ya tiene el prefijo (tokens emitidos antes
                        // de la unificación), no lo duplicamos. Compatibilidad temporal.
                        String authority = rol.startsWith("ROLE_") ? rol : "ROLE_" + rol;
                        return new SimpleGrantedAuthority(authority);
                    })
                    .toList();

            // Paso 5: Crear el token de autenticación de Spring Security.
            // Las credenciales son null porque la firma del JWT ya las valida.
            UsernamePasswordAuthenticationToken autenticacion =
                    new UsernamePasswordAuthenticationToken(email, null, autoridades);

            // Enriquecer la autenticación con metadatos de la petición (IP, session).
            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Paso 6: Registrar la autenticación en el contexto de seguridad de Spring.
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
        }

        // Paso 7: Continuar la cadena de filtros independientemente de si el token era válido.
        filterChain.doFilter(request, response);

    }

    /**
     * El sistema extrae el valor del token JWT desde el header HTTP {@code Authorization}.
     *
     * @param request La petición HTTP entrante.
     * @return El token JWT sin el prefijo "Bearer ", o {@code null} si el header está ausente.
     */
    private String extraerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        // El header debe comenzar con "Bearer " para ser un token JWT válido.
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7); // Retorna solo el token, eliminando "Bearer ".
        }
        return null;
    }
}
