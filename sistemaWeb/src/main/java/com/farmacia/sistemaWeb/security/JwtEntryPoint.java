package com.farmacia.sistemaWeb.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * JwtEntryPoint — Manejador de errores para peticiones no autorizadas (HTTP 401).
 *
 * <p>Spring Security invoca este componente cuando una petición llega sin token JWT válido
 * o con un token expirado a un endpoint protegido. El sistema responde con un cuerpo JSON
 * estructurado para que el {@code ErrorInterceptor} de Angular lo procese limpiamente.</p>
 *
 * <p>Cambios clave respecto a la versión anterior:</p>
 * <ul>
 *   <li>En lugar de {@code response.sendError()} (que retorna HTML), el sistema escribe
 *       directamente un JSON en el body de la respuesta.</li>
 *   <li>El cuerpo JSON incluye {@code timestamp}, {@code status}, {@code error} y
 *       {@code mensaje} para facilitar el diagnóstico en frontend y logs.</li>
 * </ul>
 */
@Component
public class JwtEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * El sistema invoca este método cuando Spring Security detecta que la petición
     * no está autenticada y el endpoint lo requiere.
     *
     * <p>El método escribe una respuesta JSON con status 401 directamente en el
     * {@link HttpServletResponse}, evitando que Spring genere una página de error HTML.</p>
     *
     * @param request       La petición HTTP que generó el error de autenticación.
     * @param response      La respuesta HTTP donde se escribe el error JSON.
     * @param authException La excepción de autenticación que disparó este punto de entrada.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // El sistema configura el tipo de contenido y el status HTTP 401.
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // El sistema construye el cuerpo de error como un Map que se serializa a JSON.
        Map<String, Object> cuerpoError = Map.of(
                "timestamp",  Instant.now().toString(),
                "status",     HttpServletResponse.SC_UNAUTHORIZED,
                "error",      "No autorizado",
                "mensaje",    "El token JWT es inválido, ha expirado, o no fue proporcionado.",
                "path",       request.getRequestURI()
        );

        // ObjectMapper serializa el Map directamente al OutputStream de la respuesta.
        objectMapper.writeValue(response.getOutputStream(), cuerpoError);
    }
}
