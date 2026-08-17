package com.farmacia.sistemaWeb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JwtProvider — Servicio centralizado para la generación y validación del token JWT.
 *
 * <p>El proveedor opera como la fuente única de verdad para todo lo relacionado con JWT en
 * el sistema PetyZoos. Elimina cualquier implementación secundaria y consolida la lógica en
 * este único componente, compatible con Java 21 y la API moderna de JJWT (0.11.x+).</p>
 *
 * <p>Cambios clave respecto a la versión anterior:</p>
 * <ul>
 *   <li>{@code SignatureAlgorithm.HS512} (deprecated) es reemplazado por {@code Jwts.SIG.HS512}.</li>
 *   <li>{@code .setSubject()} (deprecated) es reemplazado por {@code .subject()}.</li>
 *   <li>{@code parserBuilder()} (deprecated) es reemplazado por {@code parser()}.</li>
 *   <li>{@code parseClaimsJws()} (deprecated) es reemplazado por {@code parseSignedClaims()}.</li>
 *   <li>El token ahora incluye el claim {@code "roles"} para soportar RBAC en el filtro de seguridad.</li>
 * </ul>
 */
@Component
public class JwtProvider {

    // El secreto se inyecta desde application.properties — nunca se hardcodea en código.
    @Value("${jwt.secret}")
    private String secret;

    // El tiempo de expiración se configura en milisegundos (86400000 = 24 horas).
    @Value("${jwt.expirationMs}")
    private long expirationMs;

    // La clave criptográfica se construye una sola vez al arrancar el contexto de Spring.
    private SecretKey secretKey;

    /**
     * El sistema construye la clave HMAC-SHA512 a partir del secreto configurado.
     * Se ejecuta automáticamente después de la inyección de dependencias.
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // Keys.hmacShaKeyFor garantiza que la clave tenga el tamaño mínimo para HS512 (512 bits).
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * El sistema genera un token JWT firmado que incluye el email del usuario y su rol.
     *
     * @param authentication Objeto de autenticación de Spring Security ya verificado.
     * @return Token JWT como cadena de texto compacta (header.payload.signature).
     */
    public String generarToken(Authentication authentication) {
        String email = authentication.getName();
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        // El sistema extrae todos los roles del usuario autenticado.
        // Los roles se normalizan eliminando el prefijo ROLE_ si existe,
        // de modo que el token siempre almacena ["ADMIN"] y no ["ROLE_ADMIN"].
        // El JwtFilter es el único punto responsable de añadir el prefijo ROLE_
        // al reconstruir las GrantedAuthority desde el token.
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r) // normalizar: ROLE_ADMIN → ADMIN
                .toList();


        return Jwts.builder()
                // API moderna Java 21 / JJWT 0.12.x — sin métodos deprecated.
                .subject(email)
                .issuedAt(ahora)
                .expiration(expiracion)
                .claim("roles", roles) // Claim personalizado para RBAC.
                .signWith(secretKey)   // El algoritmo HS512 se infiere automáticamente del tipo de clave.
                .compact();
    }

    /**
     * El sistema extrae el email (subject) del token JWT.
     *
     * @param token Token JWT en formato compacto.
     * @return Email del usuario, o {@code null} si el token es inválido.
     */
    public String getEmailFromToken(String token) {
        try {
            return parsearClaims(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * El sistema extrae la lista de roles embebidos en el token.
     *
     * @param token Token JWT en formato compacto.
     * @return Lista de roles (ej: ["ROLE_ADMIN", "ROLE_VETERINARIO"]), o lista vacía si falla.
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Object rawRoles = parsearClaims(token).get("roles");
            if (rawRoles instanceof List<?> list) { // Pattern matching for instanceof (Java 16+)
                return list.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .toList();
            }
            return List.of();
        } catch (JwtException | IllegalArgumentException e) {
            return List.of();
        }
    }

    /**
     * El sistema valida el token JWT verificando firma y fecha de expiración.
     *
     * @param token Token JWT en formato compacto.
     * @return {@code true} si el token es válido, {@code false} en cualquier otro caso.
     */
    public boolean validarToken(String token) {
        try {
            parsearClaims(token); // Si no lanza excepción, el token es válido.
            return true;
        } catch (ExpiredJwtException e) {
            // El token ha expirado — el cliente debe solicitar un nuevo inicio de sesión.
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // Token malformado, firma inválida, o argumento nulo.
            return false;
        }
    }

    /**
     * El sistema parsea y verifica la firma del token, retornando los Claims internos.
     * Este método es privado y solo es usado internamente para evitar duplicar lógica.
     *
     * @param token Token JWT en formato compacto.
     * @return {@link Claims} con toda la información del payload del token.
     * @throws JwtException Si el token es inválido, expirado, o la firma no coincide.
     */
    private Claims parsearClaims(String token) {
        // API moderna sin deprecated: .parser() + .verifyWith() + .parseSignedClaims()
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}