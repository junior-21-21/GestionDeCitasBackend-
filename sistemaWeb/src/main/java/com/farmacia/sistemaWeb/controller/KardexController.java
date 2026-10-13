package com.farmacia.sistemaWeb.controller;

import com.farmacia.sistemaWeb.dto.KardexDTO;
import com.farmacia.sistemaWeb.service.KardexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kardex")
public class KardexController {

    @Autowired
    private KardexService kardexService;

    @GetMapping("/producto/{productoId}")
    public List<KardexDTO> obtenerHistorial(@PathVariable Long productoId) {
        return kardexService.obtenerHistorialPorProducto(productoId);
    }
}
