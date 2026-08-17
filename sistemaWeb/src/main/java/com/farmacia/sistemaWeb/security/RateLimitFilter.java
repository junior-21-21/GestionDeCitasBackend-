package com.farmacia.sistemaWeb.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;

/**
 * Filtro de limite de intentos para prevenir ataques de fuerza bruta.
 * Bloquea IPs que excedan el maximo de intentos de login en un periodo de
 * tiempo.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Maximo de intentos permitidos por IP
    private static final int MAX_INTENTOS = 5;
    // Ventana de tiempo en milisegundos (15 minutos)
    private static final long VENTANA_TIEMPO_MS = 15 * 60 * 1000;

    // Almacena los intentos por IP
    private final Map<String, int[]> intentosPorIp = new ConcurrentHashMap<>();
    // Almacena timestamp del primer intento por IP
    private final Map<String, Long> tiempoPorIp = new ConcurrentHashMap<>();

    // Ejecutor para limpiar IPs expiradas y evitar memory leaks
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public RateLimitFilter() {
        // El sistema ejecuta la limpieza cada 15 minutos en background
        scheduler.scheduleAtFixedRate(this::limpiarIpsExpiradas, 15, 15, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
    }

    /**
     * Tarea en background para limpiar del mapa las IPs cuyos registros ya expiraron.
     * Previene la saturación de RAM ante escaneos distribuidos o múltiples IPs.
     */
    private void limpiarIpsExpiradas() {
        long ahora = System.currentTimeMillis();
        tiempoPorIp.entrySet().removeIf(entry -> {
            if ((ahora - entry.getValue()) > VENTANA_TIEMPO_MS) {
                intentosPorIp.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Solo aplicar rate limit al endpoint de login
        if (request.getRequestURI().equals("/api/auth/login") && "POST".equalsIgnoreCase(request.getMethod())) {
            String ip = obtenerIp(request);
            
            // Excepción de testing para localhost
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip)) {
                filterChain.doFilter(request, response);
                return;
            }

            long ahora = System.currentTimeMillis();

            // Limpiar si paso la ventana de tiempo
            Long primerIntento = tiempoPorIp.get(ip);
            if (primerIntento != null && (ahora - primerIntento) > VENTANA_TIEMPO_MS) {
                intentosPorIp.remove(ip);
                tiempoPorIp.remove(ip);
            }

            // Contar intento
            int[] contador = intentosPorIp.computeIfAbsent(ip, k -> new int[] { 0 });
            tiempoPorIp.putIfAbsent(ip, ahora);
            contador[0]++;

            if (contador[0] > MAX_INTENTOS) {
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter()
                        .write("{\"error\": \"Demasiados intentos de inicio de sesion. Intenta en 15 minutos.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String obtenerIp(HttpServletRequest request) {
        // Obtener IP real si hay un proxy/load balancer
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
