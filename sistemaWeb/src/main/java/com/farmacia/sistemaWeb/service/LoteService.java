package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.Lote;
import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.repository.LoteRepository;
import com.farmacia.sistemaWeb.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional
    public void descontarStock(Long productoId, int cantidadADescontar) {
        if (cantidadADescontar <= 0) return;

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (producto.getStock() < cantidadADescontar) {
            throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        List<Lote> lotes = loteRepository.findLotesConStockOrderByFechaVencimiento(productoId);
        int cantidadRestante = cantidadADescontar;

        if (!lotes.isEmpty()) {
            for (Lote lote : lotes) {
                if (cantidadRestante <= 0) break;

                int stockLote = lote.getStockActual();
                if (stockLote >= cantidadRestante) {
                    lote.setStockActual(stockLote - cantidadRestante);
                    cantidadRestante = 0;
                } else {
                    lote.setStockActual(0);
                    cantidadRestante -= stockLote;
                }
                loteRepository.save(lote);
            }
        } else {
            // Si no hay lotes registrados pero el producto tiene stock (productos antiguos/legacy)
            // simplemente dejamos que pase, ya que la validación inicial del producto.getStock() ya fue exitosa.
            cantidadRestante = 0;
        }

        if (cantidadRestante > 0) {
            // Aún queda cantidad por descontar (probablemente stock desfasado entre Producto y Lotes)
            // Para no bloquear la venta, lo pasamos por alto ya que Producto.stock lo soporta.
            System.out.println("Advertencia: Stock de Producto mayor al stock en Lotes para producto ID " + productoId);
        }

        // Actualizar el stock total del producto
        producto.setStock(producto.getStock() - cantidadADescontar);
        productoRepository.save(producto);
    }
}
