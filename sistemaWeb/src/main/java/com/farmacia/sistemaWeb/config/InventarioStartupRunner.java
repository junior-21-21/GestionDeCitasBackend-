package com.farmacia.sistemaWeb.config;

import com.farmacia.sistemaWeb.service.CatalogoService;
import com.farmacia.sistemaWeb.service.InventarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

/**
 * Al arrancar:
 * <ol>
 *   <li>SQL Server: quita el CHECK que Hibernate creó en kardex.tipo_operacion con la lista antigua de tipos
 *       (si no, los movimientos VENCIMIENTO y ANULACION serían rechazados por la base de datos);</li>
 *   <li>migra los productos antiguos a clases y presentaciones (clase desde el tipo y la categoría,
 *       unidad base UNIDAD y presentación base de factor 1); es idempotente;</li>
 *   <li>concilia el stock de los productos antiguos con sus lotes (idempotente).</li>
 * </ol>
 */
@Component
@Order(100)
public class InventarioStartupRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InventarioStartupRunner.class);

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CatalogoService catalogoService;

    @Override
    public void run(String... args) {
        quitarCheckTipoOperacionSqlServer();
        try {
            catalogoService.migrar();
        } catch (Exception e) {
            log.error("No se pudo migrar el catálogo (clases y presentaciones): {}", e.getMessage(), e);
        }
        try {
            int corregidos = inventarioService.conciliar();
            if (corregidos > 0) {
                log.info("Conciliación de inventario: {} productos igualados con sus lotes", corregidos);
            }
        } catch (Exception e) {
            log.error("No se pudo conciliar el inventario al arrancar: {}", e.getMessage(), e);
        }
    }

    private void quitarCheckTipoOperacionSqlServer() {
        try (Connection c = dataSource.getConnection()) {
            String producto = c.getMetaData().getDatabaseProductName();
            if (producto == null || !producto.toLowerCase().contains("microsoft")) {
                return;
            }
        } catch (Exception e) {
            return;
        }
        try {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            List<String> checks = jdbc.queryForList(
                    "SELECT cc.name FROM sys.check_constraints cc "
                            + "WHERE cc.parent_object_id = OBJECT_ID('kardex') "
                            + "AND cc.definition LIKE '%tipo_operacion%' AND cc.definition NOT LIKE '%ANULACION%'",
                    String.class);
            for (String nombre : checks) {
                jdbc.execute("ALTER TABLE kardex DROP CONSTRAINT [" + nombre.replace("]", "]]") + "]");
                log.info("Se quitó la restricción {} de kardex.tipo_operacion (tipos nuevos VENCIMIENTO/ANULACION)", nombre);
            }
        } catch (Exception e) {
            log.warn("No se pudo revisar la restricción de kardex.tipo_operacion: {}", e.getMessage());
        }
    }
}
