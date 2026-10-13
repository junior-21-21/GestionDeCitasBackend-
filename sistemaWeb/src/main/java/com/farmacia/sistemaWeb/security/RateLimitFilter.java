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

    // Maximo de intentos FALLIDOS permitidos por IP. Es mayor que el limite por cuenta (5)
    // porque todo el personal de la clinica puede salir a internet con la misma IP.
    private static final int MAX_INTENTOS = 10;
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
            long ahora = System.currentTimeMillis();

            // Limpiar si paso la ventana de tiempo
            Long primerIntento = tiempoPorIp.get(ip);
            if (primerIntento != null && (ahora - primerIntento) > VENTANA_TIEMPO_MS) {
                intentosPorIp.remove(ip);
                tiempoPorIp.remove(ip);
            }

            int[] contador = intentosPorIp.get(ip);
            if (contador != null && contador[0] >= MAX_INTENTOS) {
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter()
                        .write("{\"error\": \"Demasiados intentos de inicio de sesion. Intenta en 15 minutos.\"}");
                return;
            }

            filterChain.doFilter(request, response);

            // Solo cuentan los intentos fallidos: antes cada login correcto también sumaba,
            // y varios trabajadores con la misma IP se bloqueaban entre si.
            int status = response.getStatus();
            if (status == HttpServletResponse.SC_UNAUTHORIZED || status == HttpServletResponse.SC_FORBIDDEN) {
                int[] fallos = intentosPorIp.computeIfAbsent(ip, k -> new int[] { 0 });
                tiempoPorIp.putIfAbsent(ip, ahora);
                synchronized (fallos) {
                    fallos[0]++;
                }
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String obtenerIp(HttpServletRequest request) {
        // No se leen X-Forwarded-For ni X-Real-IP: cualquiera puede inventarlas en cada peticion
        // y saltarse el limite. Si la app esta detras de un proxy, server.forward-headers-strategy
        // hace que getRemoteAddr() devuelva la IP real solo cuando la cabecera viene del proxy.
        return request.getRemoteAddr();
    }
}
