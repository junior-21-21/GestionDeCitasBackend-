package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.AjusteRequestDTO;
import com.farmacia.sistemaWeb.dto.InventarioResumenDTO;
import com.farmacia.sistemaWeb.dto.LoteDTO;
import com.farmacia.sistemaWeb.entity.*;
import com.farmacia.sistemaWeb.repository.AjusteInventarioRepository;
import com.farmacia.sistemaWeb.repository.KardexRepository;
import com.farmacia.sistemaWeb.repository.LoteRepository;
import com.farmacia.sistemaWeb.repository.ProductoRepository;
import com.farmacia.sistemaWeb.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Núcleo del inventario. Toda variación de stock pasa por aquí para que se cumpla siempre:
 * <ul>
 *   <li>stock del producto = suma del stock de sus lotes (productos que no son SERVICIO);</li>
 *   <li>cada variación deja un movimiento en el kardex con usuario, lote, signo y documento de origen;</li>
 *   <li>el producto se bloquea (PESSIMISTIC_WRITE) mientras se modifica su stock.</li>
 * </ul>
 */
@Service
public class InventarioService {

    private static final Logger log = LoggerFactory.getLogger(InventarioService.class);

    public static final String ENTRADA = "ENTRADA";
    public static final String SALIDA = "SALIDA";

    /** Motivos de ajuste aceptados (clave → texto). */
    public static final Map<String, String> MOTIVOS;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("MERMA", "Merma");
        m.put("ROTURA", "Rotura");
        m.put("VENCIDO", "Vencido");
        m.put("CONTEO_FISICO", "Conteo físico");
        m.put("DONACION", "Donación");
        m.put("OTRO", "Otro");
        MOTIVOS = Collections.unmodifiableMap(m);
    }

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired private ProductoRepository productoRepository;
    @Autowired private LoteRepository loteRepository;
    @Autowired private KardexRepository kardexRepository;
    @Autowired private AjusteInventarioRepository ajusteRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    // ─────────────────────────── Utilidades ───────────────────────────

    /** Usuario de la sesión actual (email = nombre del principal) o null si no hay sesión (p. ej. al arrancar). */
    public Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken || auth.getName() == null) {
            return null;
        }
        return usuarioRepository.findByEmail(auth.getName()).orElse(null);
    }

    /** Bloquea el producto (SELECT ... FOR UPDATE) hasta el fin de la transacción actual. */
    @Transactional
    public Producto bloquearProducto(Long productoId) {
        if (productoId == null) {
            throw new IllegalArgumentException("Debe indicar el producto");
        }
        return productoRepository.findByIdParaActualizar(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
    }

    /** Bloquea varios productos siempre en el mismo orden (por id) para evitar bloqueos cruzados entre dos ventas. */
    @Transactional
    public Map<Long, Producto> bloquearProductos(Collection<Long> ids) {
        Map<Long, Producto> mapa = new LinkedHashMap<>();
        new TreeSet<>(ids).forEach(id -> mapa.put(id, bloquearProducto(id)));
        return mapa;
    }

    public boolean controlaStock(Producto p) {
        return p.getTipo() != Producto.TipoProducto.SERVICIO;
    }

    /**
     * Lote y vencimiento obligatorios según la regla guardada en el producto (por defecto, la de su clase:
     * medicamentos, vacunas y alimentos sí; insumos clínicos e higiene opcional; accesorios no).
     */
    public boolean requiereVencimiento(Producto p) {
        if (p.getRequiereLote() != null) return p.getRequiereLote();
        return p.getClase() != null && p.getClase().isLotePorDefecto();
    }

    private String motivoLote(Producto p) {
        return p.getClase() != null ? p.getClase().getEtiqueta().toLowerCase(Locale.ROOT) : "producto con control de lote";
    }

    /** Valida la fecha de vencimiento de un ingreso y devuelve la que se guardará (centinela si no tiene). */
    public LocalDate normalizarVencimiento(Producto p, LocalDate vencimiento) {
        if (vencimiento == null) {
            if (requiereVencimiento(p)) {
                throw new IllegalArgumentException("La fecha de vencimiento es obligatoria para «" + p.getNombre()
                        + "» (" + motivoLote(p) + ": exige lote y vencimiento)");
            }
            return Lote.SIN_VENCIMIENTO;
        }
        if (vencimiento.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de vencimiento de «" + p.getNombre() + "» ("
                    + vencimiento.format(FECHA) + ") ya pasó: no se puede ingresar mercadería vencida");
        }
        return vencimiento;
    }

    /**
     * Valida lote y vencimiento de un ingreso que crea un lote nuevo (compra, stock inicial, ajuste de entrada).
     * @return el vencimiento a guardar
     */
    public LocalDate validarIngreso(Producto p, String codigoLote, LocalDate vencimiento) {
        boolean sinLote = codigoLote == null || codigoLote.isBlank();
        if (requiereVencimiento(p) && sinLote && vencimiento == null) {
            throw new IllegalArgumentException("«" + p.getNombre() + "» (" + motivoLote(p)
                    + ") exige número de lote y fecha de vencimiento");
        }
        if (requiereVencimiento(p) && sinLote) {
            throw new IllegalArgumentException("El número de lote es obligatorio para «" + p.getNombre()
                    + "» (" + motivoLote(p) + ": exige lote y vencimiento)");
        }
        return normalizarVencimiento(p, vencimiento);
    }

    /** "tabletas", "g", "unidades"... para los mensajes. */
    public String unidades(Producto p) {
        return (p.getUnidadBase() != null ? p.getUnidadBase() : UnidadBase.UNIDAD).getPlural();
    }

    private int stockEnLotes(Long productoId) {
        Long total = loteRepository.sumStockByProductoId(productoId);
        return total == null ? 0 : total.intValue();
    }

    private Kardex movimiento(Producto p, Lote lote, int cantidad, String sentido, Kardex.TipoOperacion tipo,
                              String detalle, String docTipo, String docId, int saldo) {
        Kardex k = new Kardex();
        k.setProducto(p);
        k.setLote(lote);
        k.setCantidad(Math.abs(cantidad));
        k.setMovimiento(sentido);
        k.setTipoOperacion(tipo);
        k.setDetalle(detalle != null && detalle.length() > 255 ? detalle.substring(0, 255) : detalle);
        k.setSaldoResultante(saldo);
        k.setDocumentoTipo(docTipo);
        k.setDocumentoId(docId);
        k.setUsuario(usuarioActual());
        return kardexRepository.save(k);
    }

    // ─────────────────────────── Salidas ───────────────────────────

    /**
     * Descuenta stock por FEFO (primero el que vence antes) saltando lotes vencidos.
     * Si no alcanza, rechaza la operación con un mensaje claro. El producto debe estar bloqueado.
     * Los servicios no descuentan stock.
     */
    @Transactional
    public List<Kardex> descontarFefo(Producto p, int cantidad, Kardex.TipoOperacion tipo, String detalle,
                                      String docTipo, String docId) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (!controlaStock(p)) {
            return List.of();
        }
        if (!p.isActivo()) {
            throw new IllegalArgumentException("El producto «" + p.getNombre() + "» está desactivado");
        }
        List<Lote> vigentes = loteRepository.findLotesVigentesFefo(p.getId(), LocalDate.now());
        int disponible = vigentes.stream().mapToInt(Lote::getStockActual).sum();
        int total = stockEnLotes(p.getId());
        if (disponible < cantidad) {
            int vencidas = total - disponible;
            String u = unidades(p);
            String msg = "Stock insuficiente para «" + p.getNombre() + "»: se pidieron " + cantidad + " " + u
                    + " y hay " + disponible + " disponible" + (disponible == 1 ? "" : "s");
            if (vencidas > 0) {
                msg += disponible == 0
                        ? ". Solo queda stock vencido (" + vencidas + " " + u + "), que no se puede vender ni usar"
                        : " (además hay " + vencidas + " " + u + " vencidas que no se pueden vender ni usar)";
            }
            throw new IllegalArgumentException(msg);
        }
        List<Kardex> movimientos = new ArrayList<>();
        int restante = cantidad;
        int saldo = total;
        for (Lote lote : vigentes) {
            if (restante <= 0) break;
            int toma = Math.min(restante, lote.getStockActual());
            if (toma <= 0) continue;
            lote.setStockActual(lote.getStockActual() - toma);
            loteRepository.save(lote);
            restante -= toma;
            saldo -= toma;
            movimientos.add(movimiento(p, lote, toma, SALIDA, tipo, detalle, docTipo, docId, saldo));
        }
        p.setStock(saldo);
        productoRepository.save(p);
        return movimientos;
    }

    /** Descuenta de un lote concreto (vacuna aplicada, ajuste, baja). Bloquea el producto del lote. */
    @Transactional
    public Kardex descontarDeLote(Long loteId, int cantidad, boolean permitirVencido, Kardex.TipoOperacion tipo,
                                  String detalle, String docTipo, String docId) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        Long productoId = loteRepository.findProductoIdByLoteId(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        Producto p = bloquearProducto(productoId);
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        if (!permitirVencido && lote.isVencido()) {
            throw new IllegalArgumentException("El lote " + lote.getCodigoLote() + " está vencido ("
                    + lote.getFechaVencimiento().format(FECHA) + ") y no se puede usar");
        }
        int stockLote = lote.getStockActual() == null ? 0 : lote.getStockActual();
        if (stockLote < cantidad) {
            throw new IllegalArgumentException("El lote " + lote.getCodigoLote() + " de «" + p.getNombre()
                    + "» solo tiene " + stockLote + " u. (se pidieron " + cantidad + ")");
        }
        int total = stockEnLotes(p.getId());
        lote.setStockActual(stockLote - cantidad);
        loteRepository.save(lote);
        int saldo = total - cantidad;
        p.setStock(saldo);
        productoRepository.save(p);
        return movimiento(p, lote, cantidad, SALIDA, tipo, detalle, docTipo, docId, saldo);
    }

    // ─────────────────────────── Entradas ───────────────────────────

    /** Ingresa stock creando un lote nuevo. El producto debe estar bloqueado y el vencimiento ya normalizado. */
    @Transactional
    public Lote ingresarNuevoLote(Producto p, int cantidad, String codigoLote, LocalDate vencimiento,
                                  Kardex.TipoOperacion tipo, String detalle, String docTipo, String docId) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (!controlaStock(p)) {
            throw new IllegalArgumentException("«" + p.getNombre() + "» es un servicio: no maneja stock");
        }
        int total = stockEnLotes(p.getId());
        Lote lote = new Lote();
        lote.setProducto(p);
        lote.setCodigoLote(codigoLote.length() > 50 ? codigoLote.substring(0, 50) : codigoLote);
        lote.setFechaVencimiento(vencimiento == null ? Lote.SIN_VENCIMIENTO : vencimiento);
        lote.setCantidadInicial(cantidad);
        lote.setStockActual(cantidad);
        lote = loteRepository.save(lote);
        int saldo = total + cantidad;
        p.setStock(saldo);
        productoRepository.save(p);
        movimiento(p, lote, cantidad, ENTRADA, tipo, detalle, docTipo, docId, saldo);
        return lote;
    }

    /** Devuelve unidades a un lote existente (ajuste de entrada, anulación de venta). El producto debe estar bloqueado. */
    @Transactional
    public Kardex ingresarEnLote(Lote lote, int cantidad, Kardex.TipoOperacion tipo, String detalle,
                                 String docTipo, String docId) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        Producto p = lote.getProducto();
        int total = stockEnLotes(p.getId());
        lote.setStockActual((lote.getStockActual() == null ? 0 : lote.getStockActual()) + cantidad);
        loteRepository.save(lote);
        int saldo = total + cantidad;
        p.setStock(saldo);
        productoRepository.save(p);
        return movimiento(p, lote, cantidad, ENTRADA, tipo, detalle, docTipo, docId, saldo);
    }

    // ─────────────────────────── Ajustes ───────────────────────────

    @Transactional
    public AjusteInventario registrarAjuste(AjusteRequestDTO req) {
        if (req == null || req.getProductoId() == null) {
            throw new IllegalArgumentException("Debe indicar el producto a ajustar");
        }
        String tipo = req.getTipo() == null ? "" : req.getTipo().trim().toUpperCase(Locale.ROOT);
        if (!ENTRADA.equals(tipo) && !SALIDA.equals(tipo)) {
            throw new IllegalArgumentException("El tipo de ajuste debe ser ENTRADA o SALIDA");
        }
        if (req.getCantidad() == null || req.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad del ajuste debe ser mayor a 0");
        }
        String motivo = req.getMotivo() == null ? "" : req.getMotivo().trim().toUpperCase(Locale.ROOT);
        if (!MOTIVOS.containsKey(motivo)) {
            throw new IllegalArgumentException("El motivo es obligatorio (merma, rotura, vencido, conteo físico, donación u otro)");
        }
        String observacion = req.getObservacion() == null ? null : req.getObservacion().trim();
        if ("OTRO".equals(motivo) && (observacion == null || observacion.isEmpty())) {
            throw new IllegalArgumentException("Describa el motivo del ajuste en la observación");
        }
        if (observacion != null && observacion.length() > 255) {
            observacion = observacion.substring(0, 255);
        }

        Producto p = bloquearProducto(req.getProductoId());
        if (!controlaStock(p)) {
            throw new IllegalArgumentException("«" + p.getNombre() + "» es un servicio: no maneja stock");
        }

        AjusteInventario ajuste = new AjusteInventario();
        ajuste.setProducto(p);
        ajuste.setTipo(tipo);
        ajuste.setCantidad(req.getCantidad());
        ajuste.setMotivo(motivo);
        ajuste.setObservacion(observacion == null || observacion.isEmpty() ? null : observacion);
        ajuste.setUsuario(usuarioActual());
        ajuste = ajusteRepository.save(ajuste);

        String docId = String.valueOf(ajuste.getId());
        Kardex.TipoOperacion tipoKardex = "VENCIDO".equals(motivo) && SALIDA.equals(tipo)
                ? Kardex.TipoOperacion.VENCIMIENTO : Kardex.TipoOperacion.AJUSTE;
        String detalle = (SALIDA.equals(tipo) ? "Ajuste de salida" : "Ajuste de entrada") + " · " + MOTIVOS.get(motivo)
                + (ajuste.getObservacion() != null ? ": " + ajuste.getObservacion() : "");

        Lote lote;
        if (SALIDA.equals(tipo)) {
            if (req.getLoteId() == null) {
                throw new IllegalArgumentException("Seleccione el lote del que sale la mercadería");
            }
            lote = loteDelProducto(req.getLoteId(), p);
            descontarDeLote(lote.getId(), req.getCantidad(), true, tipoKardex, detalle, "AJUSTE", docId);
        } else if (req.getLoteId() != null) {
            lote = loteDelProducto(req.getLoteId(), p);
            if (lote.isVencido()) {
                throw new IllegalArgumentException("El lote " + lote.getCodigoLote() + " está vencido: no se le puede ingresar stock");
            }
            ingresarEnLote(lote, req.getCantidad(), tipoKardex, detalle, "AJUSTE", docId);
        } else {
            LocalDate venc = validarIngreso(p, req.getCodigoLote(), req.getFechaVencimiento());
            String codigo = req.getCodigoLote() == null || req.getCodigoLote().isBlank()
                    ? "AJ-" + ajuste.getId() : req.getCodigoLote().trim();
            lote = ingresarNuevoLote(p, req.getCantidad(), codigo, venc, tipoKardex, detalle, "AJUSTE", docId);
        }
        ajuste.setLote(lote);
        return ajusteRepository.save(ajuste);
    }

    private Lote loteDelProducto(Long loteId, Producto p) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        if (!lote.getProducto().getId().equals(p.getId())) {
            throw new IllegalArgumentException("El lote " + lote.getCodigoLote() + " no pertenece a «" + p.getNombre() + "»");
        }
        return lote;
    }

    /** Da de baja todo el stock de un lote vencido (ajuste de salida por vencimiento). */
    @Transactional
    public AjusteInventario darDeBajaLote(Long loteId, String observacion) {
        Long productoId = loteRepository.findProductoIdByLoteId(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        bloquearProducto(productoId);
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        if (!lote.isVencido()) {
            throw new IllegalArgumentException("El lote " + lote.getCodigoLote()
                    + " no está vencido. Para retirar unidades vigentes use «Ajustar stock»");
        }
        if (lote.getStockActual() == null || lote.getStockActual() <= 0) {
            throw new IllegalArgumentException("El lote " + lote.getCodigoLote() + " ya no tiene stock");
        }
        AjusteRequestDTO req = new AjusteRequestDTO();
        req.setProductoId(productoId);
        req.setTipo(SALIDA);
        req.setCantidad(lote.getStockActual());
        req.setLoteId(loteId);
        req.setMotivo("VENCIDO");
        String obs = "Baja del lote " + lote.getCodigoLote() + " vencido el " + lote.getFechaVencimiento().format(FECHA);
        if (observacion != null && !observacion.isBlank()) {
            obs += " · " + observacion.trim();
        }
        req.setObservacion(obs);
        return registrarAjuste(req);
    }

    // ─────────────────────────── Conciliación ───────────────────────────

    /**
     * Iguala el stock de cada producto con la suma de sus lotes. Es idempotente:
     * <ul>
     *   <li>si el producto tiene más stock que lotes (productos antiguos sin lote), crea el lote "INICIAL-&lt;código&gt;"
     *       con la diferencia y un movimiento de ingreso;</li>
     *   <li>si los lotes suman más que el stock, el stock pasa a ser la suma de los lotes (con su movimiento).</li>
     * </ul>
     * @return cantidad de productos corregidos
     */
    @Transactional
    public int conciliar() {
        int corregidos = 0;
        for (Producto resumen : productoRepository.findAll()) {
            if (!controlaStock(resumen)) continue;
            int enLotes = stockEnLotes(resumen.getId());
            int stock = resumen.getStock() == null ? 0 : resumen.getStock();
            if (enLotes == stock) continue;

            Producto p = bloquearProducto(resumen.getId());
            enLotes = stockEnLotes(p.getId());
            stock = p.getStock() == null ? 0 : Math.max(0, p.getStock());
            if (stock > enLotes) {
                int diferencia = stock - enLotes;
                Lote lote = new Lote();
                lote.setProducto(p);
                lote.setCodigoLote(("INICIAL-" + p.getCodigo()).length() > 50
                        ? ("INICIAL-" + p.getCodigo()).substring(0, 50) : "INICIAL-" + p.getCodigo());
                lote.setFechaVencimiento(Lote.SIN_VENCIMIENTO);
                lote.setCantidadInicial(diferencia);
                lote.setStockActual(diferencia);
                lote = loteRepository.save(lote);
                p.setStock(stock);
                productoRepository.save(p);
                movimiento(p, lote, diferencia, ENTRADA, Kardex.TipoOperacion.INGRESO,
                        "Saldo inicial sin lote (conciliación de inventario)", "CONCILIACION", null, stock);
            } else {
                int diferencia = enLotes - stock;
                p.setStock(enLotes);
                productoRepository.save(p);
                movimiento(p, null, diferencia, ENTRADA, Kardex.TipoOperacion.AJUSTE,
                        "Conciliación: el stock se igualó a la suma de sus lotes", "CONCILIACION", null, enLotes);
            }
            corregidos++;
            log.info("Inventario conciliado para {} ({}): stock={} lotes={}", p.getNombre(), p.getCodigo(), stock, enLotes);
        }
        return corregidos;
    }

    // ─────────────────────────── Consultas ───────────────────────────

    @Transactional(readOnly = true)
    public List<LoteDTO> lotesDeProducto(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new IllegalArgumentException("Producto no encontrado: " + productoId);
        }
        return loteRepository.findByProductoIdOrdenados(productoId).stream()
                .map(l -> LoteDTO.from(l, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<LoteDTO> lotesPorVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        return loteRepository.findConStockVenceEntre(hoy, hoy.plusDays(Math.max(0, dias))).stream()
                .map(l -> LoteDTO.from(l, true)).toList();
    }

    @Transactional(readOnly = true)
    public List<LoteDTO> lotesVencidos() {
        return loteRepository.findVencidosConStock(LocalDate.now()).stream()
                .map(l -> LoteDTO.from(l, true)).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarAjustes(Long productoId) {
        List<AjusteInventario> ajustes = productoId != null
                ? ajusteRepository.findByProductoIdOrderByFechaDesc(productoId)
                : ajusteRepository.findTop100ByOrderByFechaDesc();
        List<Map<String, Object>> salida = new ArrayList<>();
        for (AjusteInventario a : ajustes) {
            salida.add(ajusteComoMapa(a));
        }
        return salida;
    }

    public Map<String, Object> ajusteComoMapa(AjusteInventario a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("fecha", a.getFecha());
        m.put("productoId", a.getProducto().getId());
        m.put("productoNombre", a.getProducto().getNombre());
        m.put("tipo", a.getTipo());
        m.put("cantidad", a.getCantidad());
        m.put("motivo", a.getMotivo());
        m.put("observacion", a.getObservacion());
        m.put("loteId", a.getLote() != null ? a.getLote().getId() : null);
        m.put("loteCodigo", a.getLote() != null ? a.getLote().getCodigoLote() : null);
        m.put("stockResultante", a.getProducto().getStock());
        m.put("usuario", a.getUsuario() != null ? a.getUsuario().getNombreCompleto() : null);
        return m;
    }

    @Transactional(readOnly = true)
    public InventarioResumenDTO resumen() {
        InventarioResumenDTO r = new InventarioResumenDTO();
        BigDecimal valor = BigDecimal.ZERO;
        long unidades = 0;
        long activos = 0;
        List<InventarioResumenDTO.ProductoAlerta> bajos = new ArrayList<>();
        for (Producto p : productoRepository.findAll()) {
            if (!p.isActivo() || !controlaStock(p)) continue;
            activos++;
            int stock = p.getStock() == null ? 0 : p.getStock();
            unidades += stock;
            BigDecimal costo = p.getCostoUnitario() != null ? p.getCostoUnitario()
                    : (p.getPrecioCompra() == null ? BigDecimal.ZERO : p.getPrecioCompra());
            valor = valor.add(costo.multiply(BigDecimal.valueOf(stock)));
            int minimo = p.getStockMinimo() == null ? 0 : p.getStockMinimo();
            if (stock <= minimo) {
                bajos.add(new InventarioResumenDTO.ProductoAlerta(p.getId(), p.getCodigo(), p.getNombre(), stock, minimo));
            }
        }
        bajos.sort(Comparator.comparingInt(InventarioResumenDTO.ProductoAlerta::getStock));
        r.setValorInventario(valor.setScale(2, RoundingMode.HALF_UP));
        r.setUnidades(unidades);
        r.setProductosActivos(activos);
        r.setStockBajo(bajos);
        r.setLotesPorVencer(lotesPorVencer(LoteDTO.DIAS_ALERTA));
        r.setLotesVencidos(lotesVencidos());
        return r;
    }
}
