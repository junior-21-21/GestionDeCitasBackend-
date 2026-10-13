package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.entity.ClaseProducto;
import com.farmacia.sistemaWeb.entity.Presentacion;
import com.farmacia.sistemaWeb.entity.UnidadBase;
import com.farmacia.sistemaWeb.service.CatalogoService;
import com.farmacia.sistemaWeb.service.InventarioService;
import com.farmacia.sistemaWeb.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private CatalogoService catalogoService;

    @GetMapping
    public List<Producto> obtenerTodos() {
        return productoService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        Optional<Producto> producto = productoService.obtenerPorId(id);
        return producto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Alta. Acepta stockInicial, loteInicial y vencimientoInicial: el stock inicial entra como lote + ingreso en el kardex. */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Producto producto) {
        producto.setId(null); // un id enviado por el cliente sobrescribiría otro producto
        try {
            return ResponseEntity.ok(productoService.crear(producto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Edición. El campo stock se ignora: el stock solo cambia por compras, ventas, consumos y ajustes. */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        if (!productoService.obtenerPorId(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        try {
            return ResponseEntity.ok(productoService.actualizar(id, producto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Si el producto tiene historial se desactiva (activo=false) en lugar de borrarse. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!productoService.obtenerPorId(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        boolean desactivado = productoService.eliminar(id);
        return ResponseEntity.ok(Map.of(
                "desactivado", desactivado,
                "mensaje", desactivado
                        ? "El producto tiene movimientos, así que se desactivó en lugar de eliminarse."
                        : "Producto eliminado."));
    }

    /**
     * Conciliación de productos antiguos: crea el lote "INICIAL-&lt;código&gt;" para el stock que no tiene lote
     * (o iguala el stock a la suma de lotes). Es idempotente; también se ejecuta al arrancar.
     */
    @PostMapping("/sync-lotes-legacy")
    public ResponseEntity<?> syncLotesLegacy() {
        int count = inventarioService.conciliar();
        return ResponseEntity.ok("Se conciliaron " + count + " productos con sus lotes.");
    }

    // ─────────────────────── Clases, unidades y presentaciones ───────────────────────

    /** Clases (con sus reglas por defecto) y unidades base, para los formularios. */
    @GetMapping("/catalogo")
    public Map<String, Object> catalogo() {
        List<Map<String, Object>> clases = new ArrayList<>();
        for (ClaseProducto c : ClaseProducto.values()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("valor", c.name());
            m.put("etiqueta", c.getEtiqueta());
            m.put("linea", c.getLinea().name());
            m.put("requiereReceta", c.isRecetaPorDefecto());
            m.put("controlado", c.isControladoPorDefecto());
            m.put("refrigerado", c.isRefrigeradoPorDefecto());
            m.put("requiereLote", c.isLotePorDefecto());
            m.put("tipo", c.getTipo().name());
            clases.add(m);
        }
        List<Map<String, Object>> unidades = new ArrayList<>();
        for (UnidadBase u : UnidadBase.values()) {
            unidades.add(Map.of("valor", u.name(), "etiqueta", u.getEtiqueta(), "plural", u.getPlural(), "fraccionable", u.isFraccionable()));
        }
        return Map.of("clases", clases, "unidades", unidades);
    }

    /** Escáner: devuelve el producto y la presentación del código (o el código interno del producto → presentación base). */
    @GetMapping("/codigo-barras/{codigo}")
    public ResponseEntity<?> buscarPorCodigoBarras(@PathVariable String codigo) {
        return catalogoService.buscarPorCodigo(codigo)
                .<ResponseEntity<?>>map(p -> {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("producto", p.getProducto());
                    r.put("presentacion", p);
                    return ResponseEntity.ok(r);
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of(
                        "mensaje", "No hay ningún producto con el código " + codigo.trim())));
    }

    /** Código interno EAN-13 (prefijo 200 + dígito de control) libre, para productos sin código del fabricante. */
    @GetMapping("/codigo-barras/generar")
    public Map<String, String> generarCodigoInterno() {
        return Map.of("codigo", catalogoService.generarCodigoInterno());
    }

    @GetMapping("/{id}/presentaciones")
    public ResponseEntity<?> presentaciones(@PathVariable Long id) {
        if (productoService.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(catalogoService.presentacionesDe(id));
    }

    @PostMapping("/{id}/presentaciones")
    public ResponseEntity<?> crearPresentacion(@PathVariable Long id, @RequestBody Presentacion presentacion) {
        presentacion.setId(null);
        try {
            return ResponseEntity.ok(catalogoService.guardarUna(id, presentacion));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{id}/presentaciones/{presentacionId}")
    public ResponseEntity<?> actualizarPresentacion(@PathVariable Long id, @PathVariable Long presentacionId,
                                                    @RequestBody Presentacion presentacion) {
        presentacion.setId(presentacionId);
        try {
            return ResponseEntity.ok(catalogoService.guardarUna(id, presentacion));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
