package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.LoteDTO;
import com.farmacia.sistemaWeb.dto.MovimientoInventarioResponseDTO;
import com.farmacia.sistemaWeb.entity.Lote;
import com.farmacia.sistemaWeb.entity.MovimientoInventario;
import com.farmacia.sistemaWeb.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @PostMapping("/entrada")
    public ResponseEntity<?> registrarEntrada(@RequestParam Long usuarioId,
            @RequestParam String codigoBarras,
            @RequestBody LoteDTO loteDTO) {
        try {
            inventarioService.registrarEntrada(usuarioId, codigoBarras, loteDTO);
            return ResponseEntity.ok().body("{\"message\": \"Entrada registrada exitosamente\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/lotes/disponibles/{codigoBarras}")
    public ResponseEntity<List<Lote>> obtenerLotesDisponibles(@PathVariable String codigoBarras) {
        return ResponseEntity.ok(inventarioService.obtenerLotesDisponibles(codigoBarras));
    }

    @GetMapping("/movimientos/{codigoBarras}")
    public ResponseEntity<List<MovimientoInventarioResponseDTO>> obtenerHistorialMovimientos(
            @PathVariable String codigoBarras) {
        List<MovimientoInventario> movs = inventarioService.obtenerHistorialMovimientos(codigoBarras);
        return ResponseEntity.ok(movs.stream().map(this::mapToResponseDTO).collect(Collectors.toList()));
    }

    @GetMapping("/movimientos/controlados")
    public ResponseEntity<List<MovimientoInventarioResponseDTO>> obtenerLibroEstupefacientes() {
        List<MovimientoInventario> movs = inventarioService.obtenerLibroEstupefacientes();
        return ResponseEntity.ok(movs.stream().map(this::mapToResponseDTO).collect(Collectors.toList()));
    }

    private MovimientoInventarioResponseDTO mapToResponseDTO(MovimientoInventario m) {
        MovimientoInventarioResponseDTO dto = new MovimientoInventarioResponseDTO();
        dto.setId(m.getId());
        dto.setTipoMovimiento(m.getTipoMovimiento().name());
        dto.setCantidad(m.getCantidad());
        dto.setFechaHora(m.getFechaHora());
        dto.setMotivoAjuste(m.getMotivoAjuste());
        dto.setReferenciaId(m.getReferenciaId());

        if (m.getProducto() != null) {
            MovimientoInventarioResponseDTO.ProductoInfo pInfo = new MovimientoInventarioResponseDTO.ProductoInfo();
            pInfo.setCodigoBarras(m.getProducto().getCodigoBarras());
            pInfo.setNombre(m.getProducto().getNombre());
            pInfo.setIsControlado(m.getProducto().getIsControlado());
            dto.setProducto(pInfo);
        }

        if (m.getLote() != null) {
            MovimientoInventarioResponseDTO.LoteInfo lInfo = new MovimientoInventarioResponseDTO.LoteInfo();
            lInfo.setId(m.getLote().getId());
            lInfo.setNumeroLote(m.getLote().getNumeroLote());
            dto.setLote(lInfo);
        }

        if (m.getUsuario() != null) {
            MovimientoInventarioResponseDTO.UsuarioInfo uInfo = new MovimientoInventarioResponseDTO.UsuarioInfo();
            uInfo.setId(m.getUsuario().getId());
            uInfo.setNombres(m.getUsuario().getNombres());
            // Nota: si el Usuario no tiene campo 'apellidos' expuesto, usamos solo nombres
            // o null
            uInfo.setApellidos("");
            dto.setUsuario(uInfo);
        }

        return dto;
    }

    @PostMapping("/ajuste")
    public ResponseEntity<?> ajustarInventario(
            @RequestParam Long usuarioId,
            @RequestParam String codigoBarras,
            @RequestParam(required = false) Long loteId,
            @RequestParam int cantidad,
            @RequestParam String motivo,
            @RequestParam String tipoMovimiento) {
        try {
            com.farmacia.sistemaWeb.entity.TipoMovimiento tipo = com.farmacia.sistemaWeb.entity.TipoMovimiento
                    .valueOf(tipoMovimiento);
            inventarioService.ajustarInventario(usuarioId, codigoBarras, loteId, cantidad, motivo, tipo);
            return ResponseEntity.ok().body("{\"message\": \"Ajuste registrado exitosamente\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
