package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.CobroConsulta;
import com.farmacia.sistemaWeb.entity.Consulta;
import com.farmacia.sistemaWeb.repository.CobroConsultaRepository;
import com.farmacia.sistemaWeb.repository.ConsultaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CobroConsultaService {

    @Autowired
    private CobroConsultaRepository cobroRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    public CobroConsulta registrarCobro(String codigoConsulta, CobroConsulta cobroConsulta) {
        Optional<Consulta> consultaOpt = consultaRepository.findById(codigoConsulta);
        if (consultaOpt.isPresent()) {
            Consulta consulta = consultaOpt.get();
            // Evitar duplicados
            Optional<CobroConsulta> existente = cobroRepository.findByConsultaCodigoConsulta(codigoConsulta);
            if (existente.isPresent()) {
                throw new RuntimeException("Ya existe un cobro registrado para esta consulta.");
            }
            validarCobroNuevo(cobroConsulta);
            cobroConsulta.setConsulta(consulta);
            return cobroRepository.save(cobroConsulta);
        }
        throw new RuntimeException("Consulta no encontrada");
    }

    /**
     * El cobro llega como entidad desde el cliente: se ignoran id y estado (antes se podía enviar
     * estado "PAGADO" sin pasar por pagarCobro, o un id existente que sobrescribía otro cobro).
     */
    private void validarCobroNuevo(CobroConsulta cobro) {
        cobro.setId(null);
        cobro.setEstado("PENDIENTE");
        // La fecha la pone el servidor (antes un cobro con fecha antigua no aparecia en "hoy" ni en "mes").
        cobro.setFecha(java.time.LocalDateTime.now());
        if (cobro.getDetalleCargos() == null || cobro.getDetalleCargos().isBlank()) {
            throw new RuntimeException("El detalle de cargos es obligatorio.");
        }
        try {
            com.fasterxml.jackson.databind.JsonNode items = objectMapper.readTree(cobro.getDetalleCargos());
            if (!items.isArray() || items.isEmpty()) {
                throw new RuntimeException("El detalle de cargos debe ser una lista con al menos un cargo.");
            }
            // Precios, subtotales y total se recalculan aqui: antes se guardaban tal como llegaban y
            // se podia cobrar un medicamento a S/ 0.01 descontando igual el stock.
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (com.fasterxml.jackson.databind.JsonNode nodo : items) {
                if (!(nodo instanceof com.fasterxml.jackson.databind.node.ObjectNode item)) {
                    throw new RuntimeException("El detalle de cargos no tiene un formato válido.");
                }
                com.fasterxml.jackson.databind.JsonNode cant = item.get("cantidad");
                if (cant == null || !cant.canConvertToInt() || cant.asDouble() != Math.floor(cant.asDouble()) || cant.asInt() <= 0) {
                    throw new RuntimeException("La cantidad de cada cargo debe ser un número entero mayor a 0.");
                }
                int cantidad = cant.asInt();
                java.math.BigDecimal precio;
                String codigo = item.hasNonNull("productoId") ? item.get("productoId").asText().trim() : "";
                if (!codigo.isEmpty()) {
                    com.farmacia.sistemaWeb.entity.Producto producto = productoRepository.findByCodigo(codigo)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + codigo));
                    if (producto.getPrecioVenta() == null) {
                        throw new RuntimeException("El producto " + producto.getNombre() + " no tiene precio de venta.");
                    }
                    precio = producto.getPrecioVenta();
                } else {
                    com.fasterxml.jackson.databind.JsonNode p = item.get("precio");
                    if (p == null || !p.isNumber() || p.asDouble() < 0) {
                        throw new RuntimeException("El precio de cada cargo es obligatorio y no puede ser negativo.");
                    }
                    precio = p.decimalValue();
                }
                precio = precio.setScale(2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal subtotal = precio.multiply(java.math.BigDecimal.valueOf(cantidad));
                item.put("precio", precio);
                item.put("subtotal", subtotal);
                total = total.add(subtotal);
            }
            cobro.setDetalleCargos(objectMapper.writeValueAsString(items));
            cobro.setTotal(total.doubleValue());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("El detalle de cargos no tiene un formato válido.");
        }
    }

    public Optional<CobroConsulta> obtenerCobroPorConsulta(String codigoConsulta) {
        return cobroRepository.findByConsultaCodigoConsulta(codigoConsulta);
    }

    public CobroConsulta obtenerCobroPorId(Long id) {
        return cobroRepository.findById(id).orElseThrow(() -> new RuntimeException("Cobro no encontrado"));
    }

    public java.util.List<CobroConsulta> listarCobrosPendientes() {
        return cobroRepository.findByEstado("PENDIENTE");
    }

    @Autowired
    private com.farmacia.sistemaWeb.repository.ProductoRepository productoRepository;

    @Autowired
    private com.farmacia.sistemaWeb.service.LoteService loteService;

    @Autowired
    private com.farmacia.sistemaWeb.service.KardexService kardexService;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @org.springframework.transaction.annotation.Transactional
    public CobroConsulta pagarCobro(Long idCobro) {
        CobroConsulta cobro = cobroRepository.findByIdParaActualizar(idCobro)
                .orElseThrow(() -> new RuntimeException("Cobro no encontrado"));

        if ("PAGADO".equals(cobro.getEstado())) {
            throw new RuntimeException("El cobro ya se encuentra pagado.");
        }

        try {
            // Analizar el JSON de detalleCargos
            com.fasterxml.jackson.databind.JsonNode detalles = objectMapper.readTree(cobro.getDetalleCargos());
            if (detalles.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode item : detalles) {
                    if (item.has("productoId") && !item.get("productoId").isNull()) {
                        String codigoProducto = item.get("productoId").asText();
                        int cantidad = item.has("cantidad") ? item.get("cantidad").asInt() : 1;
                        if (codigoProducto != null && !codigoProducto.trim().isEmpty()) {
                            // Antes un codigo inexistente se ignoraba: se cobraba sin descontar stock.
                            com.farmacia.sistemaWeb.entity.Producto producto = productoRepository.findByCodigo(codigoProducto)
                                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + codigoProducto));
                            {
                                // Solo descontar stock si no es SERVICIO (los servicios no tienen stock físico)
                                if (producto.getTipo() != com.farmacia.sistemaWeb.entity.Producto.TipoProducto.SERVICIO) {
                                    // Descuento por lotes (FEFO, sin vencidos) con su movimiento en el kardex
                                    loteService.descontarStock(producto.getId(), cantidad,
                                            com.farmacia.sistemaWeb.entity.Kardex.TipoOperacion.CONSUMO_INTERNO,
                                            "Consulta Médica #" + cobro.getConsulta().getCodigoConsulta(),
                                            "CONSULTA", cobro.getConsulta().getCodigoConsulta());
                                }
                            }
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // Mensaje claro (p. ej. stock insuficiente): toda la operación se revierte
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el inventario: " + e.getMessage(), e);
        }

        cobro.setEstado("PAGADO");
        // El ingreso cuenta el dia en que se paga (dashboard: hoy / mes).
        cobro.setFecha(java.time.LocalDateTime.now());
        return cobroRepository.save(cobro);
    }
}
