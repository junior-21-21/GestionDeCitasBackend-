package com.farmacia.sistemaWeb.config;

import com.farmacia.sistemaWeb.service.HistorialDemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Genera el historial de demostración (30 días de uso simulado) al arrancar, solo si se activó con
 * {@code app.demo.historial=true} (variable de entorno {@code APP_DEMO_HISTORIAL=true}), en cualquier perfil.
 * <p>
 * Corre cuando la aplicación ya está lista (después de los seeders y de la conciliación de inventario).
 * Es idempotente: si el historial ya existe, solo lo informa en el log. Pensado para bases de demostración o
 * de prueba, NO para la base real de la clínica.
 */
@Component
public class HistorialDemoGenerator {

    private static final Logger log = LoggerFactory.getLogger(HistorialDemoGenerator.class);

    @Autowired
    private HistorialDemoService historialDemoService;

    @Value("${app.demo.historial:false}")
    private boolean activo;

    @EventListener(ApplicationReadyEvent.class)
    public void alArrancar() {
        if (!activo) {
            return;
        }
        if (historialDemoService.yaGenerado()) {
            log.info("APP_DEMO_HISTORIAL=true, pero el historial de demostración ya existe en esta base de datos: no se genera de nuevo.");
            return;
        }
        try {
            historialDemoService.generar();
            log.info("Historial de demostración listo. Quite APP_DEMO_HISTORIAL (o déjelo; no se vuelve a generar).");
        } catch (HistorialDemoService.ConflictoException e) {
            log.info("Historial de demostración: {}", e.getMessage());
        } catch (Exception e) {
            log.error("No se pudo generar el historial de demostración: {}", e.getMessage(), e);
        }
    }
}
