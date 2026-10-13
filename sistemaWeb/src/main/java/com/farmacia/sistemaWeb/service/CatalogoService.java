package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.*;
import com.farmacia.sistemaWeb.repository.CategoriaRepository;
import com.farmacia.sistemaWeb.repository.PresentacionRepository;
import com.farmacia.sistemaWeb.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.*;

/**
 * Clases de producto, presentaciones y códigos de barras.
 * <ul>
 *   <li>La clase fija la línea (farmacia/tienda), las reglas por defecto y el tipo antiguo (compatibilidad).</li>
 *   <li>Todo producto tiene al menos una presentación activa de factor 1 (la presentación base).</li>
 *   <li>El código de barras es único entre todas las presentaciones.</li>
 * </ul>
 */
@Service
public class CatalogoService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired private PresentacionRepository presentacionRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    // ─────────────────────────── Clase y reglas ───────────────────────────

    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private static boolean contiene(String texto, String... claves) {
        for (String c : claves) {
            if (texto.contains(c)) return true;
        }
        return false;
    }

    /**
     * Clase sugerida para un producto que solo tiene el tipo antiguo (migración y clientes antiguos).
     * Las categorías que suenan a medicamento o vacuna mandan; después, el tipo y la categoría.
     */
    public ClaseProducto derivarClase(Producto.TipoProducto tipo, String categoria, String nombre) {
        if (tipo == Producto.TipoProducto.SERVICIO) return ClaseProducto.SERVICIO;
        String cat = normalizar(categoria);
        String nom = normalizar(nombre);
        if (contiene(cat, "vacuna", "biologic") || nom.startsWith("vacuna")) return ClaseProducto.VACUNA;
        if (contiene(cat, "medicament", "antiparasit", "antibiot", "farmac", "desparasit")) return ClaseProducto.MEDICAMENTO_LIBRE;
        if (tipo == Producto.TipoProducto.INSUMO_MEDICO) return ClaseProducto.INSUMO_CLINICO;
        if (contiene(cat, "aliment", "comida", "snack", "croqueta", "dieta", "premio")) return ClaseProducto.ALIMENTO;
        if (contiene(cat, "higiene", "shampoo", "champu", "arena", "aseo", "estetica", "grooming", "cuidado")) return ClaseProducto.HIGIENE;
        return ClaseProducto.ACCESORIO;
    }

    private String nombreCategoria(Categoria c) {
        if (c == null) return null;
        if (c.getNombre() != null) return c.getNombre();
        return c.getId() == null ? null : categoriaRepository.findById(c.getId()).map(Categoria::getNombre).orElse(null);
    }

    /**
     * Completa clase, tipo, línea y reglas de un producto a guardar.
     * @param anterior el producto guardado (al editar) o null (al crear)
     */
    public void aplicarClase(Producto p, Producto anterior) {
        ClaseProducto clase = p.getClase();
        if (clase == null) {
            if (anterior != null && anterior.getClase() != null
                    && (p.getTipo() == null || p.getTipo() == anterior.getTipo())) {
                clase = anterior.getClase(); // un cliente antiguo que no envía la clase no la cambia
            } else if (p.getTipo() != null) {
                clase = derivarClase(p.getTipo(), nombreCategoria(p.getCategoria()), p.getNombre());
            } else {
                throw new IllegalArgumentException("La clase del producto es obligatoria");
            }
        }
        boolean cambioDeClase = anterior == null || anterior.getClase() != clase;
        p.setClase(clase);
        p.setTipo(clase.getTipo());
        p.setLineaNegocio(clase.getLinea());
        p.setRequiereReceta(regla(p.getRequiereReceta(), anterior == null ? null : anterior.getRequiereReceta(), cambioDeClase, clase.isRecetaPorDefecto()));
        p.setControlado(regla(p.getControlado(), anterior == null ? null : anterior.getControlado(), cambioDeClase, clase.isControladoPorDefecto()));
        p.setRefrigerado(regla(p.getRefrigerado(), anterior == null ? null : anterior.getRefrigerado(), cambioDeClase, clase.isRefrigeradoPorDefecto()));
        p.setRequiereLote(regla(p.getRequiereLote(), anterior == null ? null : anterior.getRequiereLote(), cambioDeClase, clase.isLotePorDefecto()));
        if (clase == ClaseProducto.SERVICIO) {
            p.setRequiereLote(false);
            p.setControlado(false);
            p.setRefrigerado(false);
            p.setRequiereReceta(false);
        }
        if (Boolean.TRUE.equals(p.getControlado())) {
            p.setRequiereReceta(true); // un controlado siempre exige receta
        }
        if (p.getUnidadBase() == null) {
            p.setUnidadBase(anterior != null && anterior.getUnidadBase() != null ? anterior.getUnidadBase() : UnidadBase.UNIDAD);
        }
    }

    private static Boolean regla(Boolean enviado, Boolean anterior, boolean cambioDeClase, boolean porDefecto) {
        if (enviado != null) return enviado;
        if (!cambioDeClase && anterior != null) return anterior;
        return porDefecto;
    }

    // ─────────────────────────── Códigos de barras ───────────────────────────

    public static String limpiarCodigo(String codigo) {
        if (codigo == null) return null;
        String c = codigo.trim();
        return c.isEmpty() ? null : c;
    }

    /** Rechaza un código de barras ya usado por otra presentación (de este u otro producto). */
    public void validarCodigoBarras(String codigo, Long presentacionId) {
        if (codigo == null) return;
        if (codigo.length() > 50) {
            throw new IllegalArgumentException("El código de barras no puede tener más de 50 caracteres");
        }
        if (!codigo.matches("[A-Za-z0-9.\\-]+")) {
            throw new IllegalArgumentException("El código de barras «" + codigo + "» solo puede tener letras, números, puntos y guiones");
        }
        for (Presentacion otra : presentacionRepository.findByCodigoBarras(codigo)) {
            if (presentacionId == null || !otra.getId().equals(presentacionId)) {
                throw new IllegalArgumentException("El código de barras " + codigo + " ya está asignado a «"
                        + otra.getProducto().getNombre() + " · " + otra.getNombre() + "»");
            }
        }
    }

    /** Busca la presentación por código de barras; si no hay, acepta el código interno del producto (presentación base). */
    @Transactional(readOnly = true)
    public Optional<Presentacion> buscarPorCodigo(String codigo) {
        String c = limpiarCodigo(codigo);
        if (c == null) return Optional.empty();
        List<Presentacion> encontradas = presentacionRepository.findByCodigoBarras(c);
        if (!encontradas.isEmpty()) return Optional.of(encontradas.get(0));
        return productoRepository.findByCodigo(c).flatMap(p -> presentacionRepository.baseDeProducto(p.getId()));
    }

    /** Código interno tipo EAN-13 con prefijo 200 (rango de uso interno) y dígito de control válido, no usado aún. */
    @Transactional(readOnly = true)
    public String generarCodigoInterno() {
        for (int intento = 0; intento < 50; intento++) {
            StringBuilder sb = new StringBuilder("200");
            for (int i = 0; i < 9; i++) sb.append(RANDOM.nextInt(10));
            String codigo = sb.append(digitoControlEan13(sb.toString())).toString();
            if (!presentacionRepository.existsByCodigoBarras(codigo)) return codigo;
        }
        throw new IllegalStateException("No se pudo generar un código interno libre; intente de nuevo");
    }

    public static int digitoControlEan13(String doceDigitos) {
        int suma = 0;
        for (int i = 0; i < 12; i++) {
            int d = doceDigitos.charAt(i) - '0';
            suma += (i % 2 == 0) ? d : d * 3;
        }
        return (10 - suma % 10) % 10;
    }

    // ─────────────────────────── Presentaciones ───────────────────────────

    @Transactional(readOnly = true)
    public List<Presentacion> presentacionesDe(Long productoId) {
        return presentacionRepository.findByProductoIdOrdenadas(productoId);
    }

    /** Presentación base (factor 1, activa) de un producto; null si no tiene (no debería pasar tras la migración). */
    public Presentacion base(Producto p) {
        return presentacionRepository.baseDeProducto(p.getId()).orElse(null);
    }

    private static BigDecimal precio(BigDecimal v) {
        return v == null ? null : v.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Guarda las presentaciones enviadas con el producto (nuevas sin id, existentes con id) y garantiza la base.
     * Las presentaciones que no vienen en la lista no se tocan; para retirar una se envía con activo=false.
     * Si la lista es null (cliente antiguo) solo se asegura la base con el precio del producto.
     * Al final el precio de venta del producto es el de su presentación base.
     */
    @Transactional
    public List<Presentacion> sincronizar(Producto producto, List<Presentacion> enviadas) {
        boolean servicio = producto.getClase() == ClaseProducto.SERVICIO;
        List<Presentacion> actuales = presentacionRepository.findByProductoIdOrdenadas(producto.getId());
        Map<Long, Presentacion> porId = new HashMap<>();
        actuales.forEach(a -> porId.put(a.getId(), a));

        if (enviadas != null) {
            Set<String> codigosEnviados = new HashSet<>();
            for (Presentacion e : enviadas) {
                if (e == null) continue;
                String nombre = e.getNombre() == null ? "" : e.getNombre().trim();
                if (nombre.isEmpty()) {
                    throw new IllegalArgumentException("Cada presentación debe tener un nombre (p. ej. «Caja x100»)");
                }
                if (nombre.length() > 60) nombre = nombre.substring(0, 60);
                if (e.getFactor() == null || e.getFactor() < 1) {
                    throw new IllegalArgumentException("La presentación «" + nombre + "» debe contener al menos 1 "
                            + producto.getUnidadBase().getEtiqueta().toLowerCase(Locale.ROOT));
                }
                if (servicio && e.getFactor() != 1) {
                    throw new IllegalArgumentException("Un servicio solo tiene la presentación base (factor 1)");
                }
                BigDecimal pv = precio(e.getPrecioVenta());
                if (pv == null && e.getFactor() == 1) pv = precio(producto.getPrecioVenta());
                if (pv == null || pv.signum() < 0) {
                    throw new IllegalArgumentException("Indique el precio de venta de la presentación «" + nombre + "»");
                }
                String codigo = limpiarCodigo(e.getCodigoBarras());
                if (codigo != null && !codigosEnviados.add(codigo)) {
                    throw new IllegalArgumentException("El código de barras " + codigo + " está repetido en dos presentaciones");
                }

                Presentacion destino;
                if (e.getId() != null) {
                    destino = porId.get(e.getId());
                    if (destino == null) {
                        throw new IllegalArgumentException("La presentación #" + e.getId() + " no pertenece a «" + producto.getNombre() + "»");
                    }
                } else {
                    destino = new Presentacion();
                    destino.setProducto(producto);
                }
                validarCodigoBarras(codigo, destino.getId());
                destino.setNombre(nombre);
                destino.setFactor(e.getFactor());
                destino.setCodigoBarras(codigo);
                destino.setPrecioVenta(pv);
                destino.setVendible(e.isVendible());
                destino.setComprable(!servicio && e.isComprable());
                destino.setActivo(e.isActivo());
                destino = presentacionRepository.save(destino);
                porId.put(destino.getId(), destino);
            }
        }

        List<Presentacion> todas = new ArrayList<>(porId.values());
        // Nombres únicos entre las activas
        Set<String> nombres = new HashSet<>();
        for (Presentacion p : todas) {
            if (p.isActivo() && !nombres.add(normalizar(p.getNombre()))) {
                throw new IllegalArgumentException("Hay dos presentaciones activas llamadas «" + p.getNombre() + "»");
            }
        }

        Presentacion base = todas.stream().filter(p -> p.isActivo() && p.getFactor() == 1)
                .min(Comparator.comparing(Presentacion::getId)).orElse(null);
        if (base == null) {
            if (enviadas != null && todas.stream().anyMatch(p -> p.getFactor() == 1 && !p.isActivo())) {
                throw new IllegalArgumentException("La presentación base (1 " + producto.getUnidadBase().getEtiqueta().toLowerCase(Locale.ROOT)
                        + ") no se puede desactivar: si no se vende suelta, desmarque «Se vende»");
            }
            base = new Presentacion();
            base.setProducto(producto);
            base.setNombre(producto.getUnidadBase().getEtiqueta());
            base.setFactor(1);
            base.setPrecioVenta(precio(producto.getPrecioVenta() == null ? BigDecimal.ZERO : producto.getPrecioVenta()));
            base.setVendible(true);
            base.setComprable(!servicio);
            base.setActivo(true);
            if (nombres.contains(normalizar(base.getNombre()))) {
                base.setNombre(base.getNombre() + " (base)");
            }
            base = presentacionRepository.save(base);
        } else if (enviadas == null && producto.getPrecioVenta() != null) {
            base.setPrecioVenta(precio(producto.getPrecioVenta())); // cliente antiguo: el precio del producto manda
            presentacionRepository.save(base);
        }
        producto.setPrecioVenta(precio(base.getPrecioVenta()));
        productoRepository.save(producto);

        List<Presentacion> resultado = presentacionRepository.findByProductoIdOrdenadas(producto.getId());
        producto.setPresentaciones(resultado);
        return resultado;
    }

    /** Alta o edición de una sola presentación (pantallas o integraciones que no envían el producto completo). */
    @Transactional
    public Presentacion guardarUna(Long productoId, Presentacion datos) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
        List<Presentacion> resultado = sincronizar(producto, List.of(datos));
        if (datos.getId() != null) {
            return resultado.stream().filter(p -> p.getId().equals(datos.getId())).findFirst().orElseThrow();
        }
        String codigo = limpiarCodigo(datos.getCodigoBarras());
        return resultado.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(datos.getNombre().trim()) && Objects.equals(p.getCodigoBarras(), codigo))
                .max(Comparator.comparing(Presentacion::getId)).orElseThrow();
    }

    /**
     * Presentación que corresponde a una línea de venta o compra.
     * Sin presentación indicada se usa la base del producto (ventas y compras de clientes antiguos).
     */
    public Presentacion resolver(Producto producto, Presentacion pedida) {
        if (pedida != null && pedida.getId() != null) {
            Presentacion p = presentacionRepository.findById(pedida.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Presentación no encontrada: " + pedida.getId()));
            if (!p.getProducto().getId().equals(producto.getId())) {
                throw new IllegalArgumentException("La presentación «" + p.getNombre() + "» no pertenece a «" + producto.getNombre() + "»");
            }
            return p;
        }
        Presentacion base = base(producto);
        if (base == null) {
            // Producto sin presentaciones (no migrado): presentación virtual de factor 1 con el precio del producto
            base = new Presentacion();
            base.setProducto(producto);
            base.setNombre((producto.getUnidadBase() != null ? producto.getUnidadBase() : UnidadBase.UNIDAD).getEtiqueta());
            base.setFactor(1);
            base.setPrecioVenta(producto.getPrecioVenta());
        }
        return base;
    }

    /** Producto de una línea: el indicado o, si solo viene la presentación (escáner), el de la presentación. */
    public Long productoIdDeLinea(Producto producto, Presentacion presentacion) {
        if (producto != null && producto.getId() != null) return producto.getId();
        if (presentacion != null && presentacion.getId() != null) {
            return presentacionRepository.findById(presentacion.getId())
                    .map(p -> p.getProducto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Presentación no encontrada: " + presentacion.getId()));
        }
        throw new IllegalArgumentException("Cada detalle debe indicar un producto o una presentación");
    }

    // ─────────────────────────── Migración ───────────────────────────

    /**
     * Migración idempotente de los productos anteriores a las clases y presentaciones:
     * asigna clase (desde el tipo y la categoría), reglas, unidad base (UNIDAD), costo unitario
     * y la presentación base de factor 1 con el precio actual.
     */
    @Transactional
    public Map<String, Integer> migrar() {
        Map<String, Integer> porClase = new TreeMap<>();
        int presentacionesCreadas = 0;
        for (Producto p : productoRepository.findAll()) {
            boolean cambio = false;
            if (p.getClase() == null) {
                ClaseProducto clase = derivarClase(p.getTipo(), p.getCategoria() != null ? p.getCategoria().getNombre() : null, p.getNombre());
                p.setClase(clase);
                porClase.merge(clase.name(), 1, Integer::sum);
                cambio = true;
            }
            ClaseProducto clase = p.getClase();
            if (p.getTipo() != clase.getTipo()) { p.setTipo(clase.getTipo()); cambio = true; }
            if (p.getLineaNegocio() == null) { p.setLineaNegocio(clase.getLinea()); cambio = true; }
            if (p.getRequiereReceta() == null) { p.setRequiereReceta(clase.isRecetaPorDefecto()); cambio = true; }
            if (p.getControlado() == null) { p.setControlado(clase.isControladoPorDefecto()); cambio = true; }
            if (p.getRefrigerado() == null) { p.setRefrigerado(clase.isRefrigeradoPorDefecto()); cambio = true; }
            if (p.getRequiereLote() == null) { p.setRequiereLote(clase.isLotePorDefecto()); cambio = true; }
            if (p.getUnidadBase() == null) { p.setUnidadBase(UnidadBase.UNIDAD); cambio = true; }
            if (p.getCostoUnitario() == null && p.getPrecioCompra() != null) {
                p.setCostoUnitario(p.getPrecioCompra().setScale(6, RoundingMode.HALF_UP));
                cambio = true;
            }
            if (cambio) productoRepository.save(p);
            if (!presentacionRepository.existsByProductoId(p.getId())) {
                Presentacion base = new Presentacion();
                base.setProducto(p);
                base.setNombre(p.getUnidadBase().getEtiqueta());
                base.setFactor(1);
                base.setPrecioVenta(precio(p.getPrecioVenta() == null ? BigDecimal.ZERO : p.getPrecioVenta()));
                base.setVendible(true);
                base.setComprable(clase != ClaseProducto.SERVICIO);
                base.setActivo(true);
                presentacionRepository.save(base);
                presentacionesCreadas++;
            }
        }
        if (!porClase.isEmpty() || presentacionesCreadas > 0) {
            log.info("Migración de catálogo: {} productos clasificados {} · {} presentaciones base creadas",
                    porClase.values().stream().mapToInt(Integer::intValue).sum(), porClase, presentacionesCreadas);
        }
        Map<String, Integer> resumen = new LinkedHashMap<>(porClase);
        resumen.put("PRESENTACIONES_CREADAS", presentacionesCreadas);
        return resumen;
    }
}
