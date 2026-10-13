package com.farmacia.sistemaWeb.config;

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
 * Antes de sembrar datos (orden 1): en SQL Server quita el CHECK que Hibernate creó en roles.nombre con la lista
 * antigua de roles (ADMIN, RECEPCIONISTA, VETERINARIO, CLIENTE). Sin esto, crear los roles ENCARGADO_ALMACEN
 * y GROOMER fallaría. Las columnas de enumerados nuevas usan converters y no tienen CHECK.
 */
@Component
@Order(1)
public class EsquemaStartupRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EsquemaStartupRunner.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) {
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
                            + "WHERE cc.parent_object_id = OBJECT_ID('roles') "
                            + "AND cc.definition LIKE '%nombre%' AND cc.definition NOT LIKE '%GROOMER%'",
                    String.class);
            for (String nombre : checks) {
                jdbc.execute("ALTER TABLE roles DROP CONSTRAINT [" + nombre.replace("]", "]]") + "]");
                log.info("Se quitó la restricción {} de roles.nombre (roles nuevos ENCARGADO_ALMACEN y GROOMER)", nombre);
            }
        } catch (Exception e) {
            log.warn("No se pudo revisar la restricción de roles.nombre: {}", e.getMessage());
        }
    }
}
