package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public List<Producto> obtenerTodos() {
        return productoService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        Optional<Producto> producto = productoService.obtenerPorId(id);
        return producto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Producto producto) {
        try {
            return ResponseEntity.ok(productoService.guardar(producto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        if (!productoService.obtenerPorId(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        producto.setId(id);
        try {
            return ResponseEntity.ok(productoService.guardar(producto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!productoService.obtenerPorId(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Autowired
    private com.farmacia.sistemaWeb.repository.ProductoRepository productoRepo;
    @Autowired
    private com.farmacia.sistemaWeb.repository.LoteRepository loteRepo;

    @PostMapping("/sync-lotes-legacy")
    public ResponseEntity<?> syncLotesLegacy() {
        List<Producto> productos = productoRepo.findAll();
        int count = 0;
        for (Producto p : productos) {
            if (p.getStock() > 0 && p.getTipo() != Producto.TipoProducto.SERVICIO) {
                // Verificar si ya tiene lotes
                List<com.farmacia.sistemaWeb.entity.Lote> lotes = loteRepo.findLotesConStockOrderByFechaVencimiento(p.getId());
                if (lotes.isEmpty()) {
                    com.farmacia.sistemaWeb.entity.Lote lote = new com.farmacia.sistemaWeb.entity.Lote();
                    lote.setProducto(p);
                    lote.setCodigoLote("LEGACY-" + p.getCodigo());
                    // Fecha generica a futuro para que no se venza rapido
                    lote.setFechaVencimiento(java.time.LocalDate.now().plusYears(1));
                    lote.setCantidadInicial(p.getStock());
                    lote.setStockActual(p.getStock());
                    // lote.setActivo(true);
                    loteRepo.save(lote);
                    count++;
                }
            }
        }
        return ResponseEntity.ok("Se sincronizaron " + count + " productos legacy con lotes.");
    }
}
