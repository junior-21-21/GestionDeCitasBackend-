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
            return ResponseEntity.badRequest().body(e.getMessage());
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
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{dni}")
    public ResponseEntity<?> actualizar(@PathVariable String dni, @Valid @RequestBody ClienteDTO dto) {
        try {
            Cliente actualizado = clienteService.actualizarCliente(dni, dto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al eliminar el cliente.");
        }
    }

    @GetMapping("/buscar/{dni}")
    public ResponseEntity<List<ClienteResponseDTO>> buscarPorDniParcial(@PathVariable String dni) {
        return ResponseEntity.ok(clienteService.buscarPorDniParcial(dni));
    }
}
