package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.ConsultaProductoDTO;
import com.farmacia.sistemaWeb.entity.Consulta;
import com.farmacia.sistemaWeb.entity.ConsultaProducto;
import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.repository.ConsultaProductoRepository;
import com.farmacia.sistemaWeb.repository.ConsultaRepository;
import com.farmacia.sistemaWeb.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultaProductoService {

    @Autowired
    private ConsultaProductoRepository consultaProductoRepository;
    @Autowired
    private ConsultaRepository consultaRepository;
    @Autowired
    private ProductoRepository productoRepository;

    public List<ConsultaProductoDTO> obtenerProductosPorConsulta(String codigoConsulta) {
        return consultaProductoRepository.findByConsultaCodigoConsulta(codigoConsulta)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ConsultaProductoDTO agregarProductoAConsulta(String codigoConsulta, ConsultaProductoDTO dto) {
        Consulta consulta = consultaRepository.findById(codigoConsulta)
                .orElseThrow(() -> new RuntimeException("Consulta no encontrada"));

        Producto producto = productoRepository.findById(dto.getCodigoBarras())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ConsultaProducto cp = new ConsultaProducto();
        cp.setConsulta(consulta);
        cp.setProducto(producto);
        cp.setCantidad(dto.getCantidad());
        cp.setIndicaciones(dto.getIndicaciones());

        return toDTO(consultaProductoRepository.save(cp));
    }

    private ConsultaProductoDTO toDTO(ConsultaProducto entity) {
        ConsultaProductoDTO dto = new ConsultaProductoDTO();
        dto.setCodigoConsulta(entity.getConsulta().getCodigoConsulta());
        dto.setCodigoBarras(entity.getProducto().getCodigoBarras());
        dto.setCantidad(entity.getCantidad());
        dto.setIndicaciones(entity.getIndicaciones());
        return dto;
    }
}
