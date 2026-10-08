package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.Kardex;
import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.entity.Usuario;
import com.farmacia.sistemaWeb.repository.KardexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KardexService {

    @Autowired
    private KardexRepository kardexRepository;

    @Transactional
    public Kardex registrarMovimiento(Producto producto, int cantidad, Kardex.TipoOperacion tipoOperacion, String detalle, Usuario usuario) {
        Kardex kardex = new Kardex();
        kardex.setProducto(producto);
        kardex.setCantidad(cantidad); // Positivo o negativo dependiendo de la operación, aunque en este caso es un valor absoluto y el saldoResultante es lo que importa
        kardex.setTipoOperacion(tipoOperacion);
        kardex.setDetalle(detalle);
        kardex.setSaldoResultante(producto.getStock());
        kardex.setUsuario(usuario);
        
        return kardexRepository.save(kardex);
    }

    public List<Kardex> obtenerHistorialPorProducto(Long productoId) {
        return kardexRepository.findByProductoIdOrderByFechaHoraDesc(productoId);
    }
}
