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
            cobroConsulta.setConsulta(consulta);
            return cobroRepository.save(cobroConsulta);
        }
        throw new RuntimeException("Consulta no encontrada");
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
        CobroConsulta cobro = cobroRepository.findById(idCobro)
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
                            Optional<com.farmacia.sistemaWeb.entity.Producto> prodOpt = productoRepository.findByCodigo(codigoProducto);
                            if (prodOpt.isPresent()) {
                                com.farmacia.sistemaWeb.entity.Producto producto = prodOpt.get();
                                // Solo descontar stock si no es SERVICIO (los servicios no tienen stock físico)
                                if (producto.getTipo() != com.farmacia.sistemaWeb.entity.Producto.TipoProducto.SERVICIO) {
                                    // 1. Descontar usando Lotes (FIFO)
                                    loteService.descontarStock(producto.getId(), cantidad);
                                    
                                    // 2. Registrar en Kardex
                                    kardexService.registrarMovimiento(
                                            producto,
                                            cantidad,
                                            com.farmacia.sistemaWeb.entity.Kardex.TipoOperacion.CONSUMO_INTERNO,
                                            "Consulta Médica #" + cobro.getConsulta().getCodigoConsulta(),
                                            null // O pasar el usuario si está disponible en el futuro
                                    );
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el inventario: " + e.getMessage(), e);
        }

        cobro.setEstado("PAGADO");
        return cobroRepository.save(cobro);
    }
}
