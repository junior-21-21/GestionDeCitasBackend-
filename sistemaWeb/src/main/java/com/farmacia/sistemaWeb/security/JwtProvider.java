package com.farmacia.sistemaWeb.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // Convertir el string en clave segura
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // *** INICIO DE LAS MODIFICACIONES ***
        // Estas líneas son para depuración. Te mostrarán qué valor está cargando Spring.
        System.out.println("--- DEBUG JWT Provider ---");
        System.out.println("Valor de 'jwt.secret' cargado: " + (secret != null ? secret.substring(0, Math.min(secret.length(), 10)) + "..." : "NULO")); // Muestra solo una parte por seguridad
        System.out.println("Valor de 'jwt.expirationMs' cargado: " + expirationMs + " milisegundos");
        System.out.println("Esto equivale a: " + (expirationMs / 1000) + " segundos");
        System.out.println("Esto equivale a: " + (expirationMs / 1000 / 60) + " minutos");
        System.out.println("Esto equivale a: " + (expirationMs / 1000 / 60 / 60) + " horas");
        System.out.println("--------------------------");
        // *** FIN DE LAS MODIFICACIONES ***
    }

    public String generarToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        // *** AÑADE ESTA LÍNEA PARA DEPURAR LA FECHA DE EXPIRACIÓN DEL TOKEN GENERADO ***
        System.out.println("DEBUG JWT: Token para '" + username + "' se generó a las: " + now + " y expirará a las: " + expiryDate);
        // ********************************************************************************

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            System.out.println("Error al obtener username del token: " + e.getMessage());
            return null; // O lanza una excepción específica si lo prefieres
        }
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token inválido: Token expirado. Mensaje: " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            System.out.println("Token inválido: JWT no soportado. Mensaje: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.out.println("Token inválido: JWT mal formado. Mensaje: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.out.println("Token inválido: Firma JWT no válida. Mensaje: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println("Token inválido: JWT vacío o nulo. Mensaje: " + e.getMessage());
            return false;
        }
    }
}