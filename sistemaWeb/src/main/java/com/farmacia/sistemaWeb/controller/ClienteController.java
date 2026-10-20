package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.ClienteDTO;
import com.farmacia.sistemaWeb.dto.ClienteResponseDTO;
import com.farmacia.sistemaWeb.entity.Cliente;
import com.farmacia.sistemaWeb.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    @Autowired
    private ClienteService clienteService;

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody ClienteDTO dto) {
        try {
            Cliente cliente = clienteService.registrarCliente(dto);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listar() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    @GetMapping("/{dni}")
    public ResponseEntity<?> obtenerPorDni(@PathVariable String dni) {
        try {
            ClienteResponseDTO cliente = clienteService.obtenerPorDni(dni);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @PutMapping("/{dni}")
    public ResponseEntity<?> actualizar(@PathVariable String dni, @Valid @RequestBody ClienteDTO dto) {
        try {
            Cliente actualizado = clienteService.actualizarCliente(dni, dto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        }
    }

    @DeleteMapping("/{dni}")
    public ResponseEntity<?> eliminar(@PathVariable String dni) {
        try {
            clienteService.eliminarCliente(dni);
            return ResponseEntity.ok("Cliente eliminado correctamente.");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar el cliente porque tiene pacientes registrados.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.farmacia.sistemaWeb.util.GlobalExceptionHandler.mensajeSeguro(e));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al eliminar el cliente.");
        }
    }

    @org.springframework.beans.factory.annotation.Value("${app.apisperu.token:}")
    private String apisperuToken;

    /**
     * Consulta de nombres por DNI en apisperu hecha desde el servidor, para que el token no viaje
     * dentro del frontend. Devuelve el mismo JSON que entrega apisperu.
     */
    @GetMapping("/consulta-dni/{dni}")
    public ResponseEntity<?> consultarDni(@PathVariable String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            return ResponseEntity.badRequest().body(java.util.Map.of("mensaje", "El DNI debe tener 8 dígitos."));
        }
        if (apisperuToken == null || apisperuToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(java.util.Map.of("mensaje", "La consulta de DNI no está configurada (APISPERU_TOKEN)."));
        }
        try {
            String json = org.springframework.web.client.RestClient.create()
                    .get()
                    .uri("https://dniruc.apisperu.com/api/v1/dni/{dni}?token={token}", dni, apisperuToken)
                    .retrieve()
                    .body(String.class);
            return ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(json);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(java.util.Map.of("mensaje", "No se pudo consultar el DNI en este momento."));
        }
    }

    @GetMapping("/buscar/{dni}")
    public ResponseEntity<List<ClienteResponseDTO>> buscarPorDniParcial(@PathVariable String dni) {
        return ResponseEntity.ok(clienteService.buscarPorDniParcial(dni));
    }
}
