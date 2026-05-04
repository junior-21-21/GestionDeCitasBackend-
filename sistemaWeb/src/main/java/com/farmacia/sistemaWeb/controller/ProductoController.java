package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.ProductoDTO;
import com.farmacia.sistemaWeb.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService service;

    @GetMapping
    public List<ProductoDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{codigoBarras}")
    public ResponseEntity<ProductoDTO> obtenerPorCodigo(@PathVariable String codigoBarras) {
        return ResponseEntity.ok(service.obtenerPorCodigo(codigoBarras));
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> guardar(@RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(service.guardar(dto));
    }

    @PutMapping("/{codigoBarras}")
    public ResponseEntity<ProductoDTO> actualizar(@PathVariable String codigoBarras, @RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(service.actualizar(codigoBarras, dto));
    }

    @DeleteMapping("/{codigoBarras}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigoBarras) {
        service.eliminar(codigoBarras);
        return ResponseEntity.noContent().build();
    }
}
