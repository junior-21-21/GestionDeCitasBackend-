package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.VentaDTO;
import com.farmacia.sistemaWeb.dto.DetalleVentaDTO;
import com.farmacia.sistemaWeb.entity.*;
import com.farmacia.sistemaWeb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService {

    @Autowired private VentaRepository ventaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private MedicamentoRepository medicamentoRepository;

    public Venta registrarVenta(VentaDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setFecha(LocalDate.now());

        List<DetalleVenta> detalles = new ArrayList<>();
        double total = 0;

        for (DetalleVentaDTO d : dto.getDetalles()) {
            Medicamento medicamento = medicamentoRepository.findById(d.getMedicamentoId())
                    .orElseThrow(() -> new RuntimeException("Medicamento no encontrado"));

            if (medicamento.getStock() < d.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + medicamento.getNombre());
            }

            medicamento.setStock(medicamento.getStock() - d.getCantidad());
            double precioItem = medicamento.getPrecio() * d.getCantidad();

            DetalleVenta det = new DetalleVenta();
            det.setCantidad(d.getCantidad());
            det.setPrecio(precioItem);
            det.setMedicamento(medicamento);
            det.setVenta(venta);

            detalles.add(det);
            total += precioItem;
        }

        venta.setTotal(total);
        venta.setDetalles(detalles);

        return ventaRepository.save(venta);
    }

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }


    public Venta obtenerPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

}
