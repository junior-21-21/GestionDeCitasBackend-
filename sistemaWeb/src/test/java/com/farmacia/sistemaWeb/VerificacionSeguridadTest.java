package com.farmacia.sistemaWeb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
// Comprueba las correcciones de seguridad: permisos por rol, rutas públicas cerradas,
// rechazo del secreto JWT de ejemplo y bloqueo de cuentas sin revelar qué correos existen.
class VerificacionSeguridadTest {
    @Autowired MockMvc mvc;
    @Autowired com.farmacia.sistemaWeb.repository.UsuarioRepository usuarioRepository;
    ObjectMapper om = new ObjectMapper();

    String registrar(String email) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("email", email, "password", "Clave123", "nombres", "Ana", "apellidos", "Ruiz"))))
                .andReturn();
        assertEquals(200, r.getResponse().getStatus(), r.getResponse().getContentAsString());
        return (String) om.readValue(r.getResponse().getContentAsString(), Map.class).get("token");
    }

    int get(String url, String token) throws Exception {
        var b = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url);
        if (token != null) b.header("Authorization", "Bearer " + token);
        return mvc.perform(b).andReturn().getResponse().getStatus();
    }

    @Test
    void clienteNoEntraAZonasInternas() throws Exception {
        String t = registrar("cliente1@x.com");
        for (String u : List.of("/api/asistencia/todas", "/api/asistencia/horarios", "/api/recetas/consulta/X",
                "/api/registro-vacunas/paciente/X", "/api/vacunas", "/api/dashboard/stats", "/api/pacientes/public/PCP000001",
                "/api/consultas/historial/public/paciente/PCP000001", "/api/cualquier-cosa")) {
            int s = get(u, t);
            System.out.println("CLIENTE " + u + " -> " + s);
            assertEquals(403, s, u);
        }
        int s = mvc.perform(post("/api/asistencia/horarios").header("Authorization", "Bearer " + t)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andReturn().getResponse().getStatus();
        assertEquals(403, s);
        assertEquals(401, get("/api/consultas/historial/public/paciente/PCP000001", null));
        assertEquals(401, get("/api/pacientes/public/PCP000001", null));
    }

    @Test
    void tokenDeCuentaInhabilitadaDejaDeServir() throws Exception {
        String t = registrar("cliente3@x.com");
        var u = usuarioRepository.findByEmail("cliente3@x.com").orElseThrow();
        String url = "/api/usuarios/" + u.getId() + "/imagen";
        assertNotEquals(401, get(url, t));
        u.setHabilitada(false);
        usuarioRepository.save(u);
        assertEquals(401, get(url, t));
    }

    @Test
    void primerAdminNoEsPublicoFueraDeDev() throws Exception {
        int s = mvc.perform(post("/api/usuarios/admin").contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("email", "intruso@x.com", "password", "Clave1234", "nombres", "X"))))
                .andReturn().getResponse().getStatus();
        assertEquals(403, s);
        assertTrue(usuarioRepository.findByEmail("intruso@x.com").isEmpty());
    }

    @Test
    void tokenConSecretoDeEjemploEsRechazado() throws Exception {
        String falso = Jwts.builder().subject("admin@x.com").claim("roles", List.of("ADMIN"))
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(Keys.hmacShaKeyFor("test-only-local-secret-change-before-production-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdef".getBytes(StandardCharsets.UTF_8)))
                .compact();
        assertEquals(401, get("/api/reportes/exportar-clientes", falso));
    }

    String login(String email, String pwd) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("email", email, "password", pwd)))).andReturn();
        return r.getResponse().getStatus() + " " + r.getResponse().getContentAsString();
    }

    @Test
    void bloqueoYMensajes() throws Exception {
        registrar("cliente2@x.com");
        String noExiste = login("nadie@x.com", "Clave999");
        String malaClave = login("cliente2@x.com", "Clave999");
        System.out.println(noExiste + "\n" + malaClave);
        assertEquals(noExiste, malaClave);
        for (int i = 0; i < 4; i++) login("cliente2@x.com", "Clave999");
        // Durante el bloqueo la respuesta no depende de la contrasena: no sirve como oraculo.
        String bloqueadoMala = login("cliente2@x.com", "Clave999");
        String bloqueadoBuena = login("cliente2@x.com", "Clave123");
        System.out.println(bloqueadoBuena);
        assertTrue(bloqueadoBuena.startsWith("403") && bloqueadoBuena.contains("CUENTA_BLOQUEADA"));
        assertEquals(bloqueadoMala, bloqueadoBuena);
    }
}
