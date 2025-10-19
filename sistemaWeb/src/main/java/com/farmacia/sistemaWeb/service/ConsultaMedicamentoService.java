package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.ConsultaMedicamentoDTO;
import com.farmacia.sistemaWeb.dto.ConsultaMedicamentoResponse;
import com.farmacia.sistemaWeb.entity.*;
import com.farmacia.sistemaWeb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultaMedicamentoService {

    @Autowired
    private ConsultaMedicamentoRepository consultaMedicamentoRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    public ConsultaMedicamentoResponse registrarMedicamentoEnConsulta(ConsultaMedicamentoDTO dto) {
        Consulta consulta = consultaRepository.findById(dto.getConsultaId())
                .orElseThrow(() -> new RuntimeException("Consulta no encontrada"));

        Medicamento medicamento = medicamentoRepository.findById(dto.getMedicamentoId())
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado"));

        ConsultaMedicamento entity = new ConsultaMedicamento();
        entity.setConsulta(consulta);
        entity.setMedicamento(medicamento);
        entity.setCantidad(dto.getCantidad());
        entity.setId(new ConsultaMedicamentoPK(dto.getConsultaId(), dto.getMedicamentoId()));

        consultaMedicamentoRepository.save(entity);

        return new ConsultaMedicamentoResponse(
                consulta.getId(),
                medicamento.getId(),
                entity.getCantidad(),
                medicamento.getNombre(),
                medicamento.getDescripcion(),
                medicamento.getPrecio()
        );
    }


    public List<ConsultaMedicamento> listarPorConsulta(Long consultaId) {
        return consultaMedicamentoRepository.findByConsultaId(consultaId);
    }





    public List<ConsultaMedicamentoResponse> obtenerPorConsultaId(Long consultaId) {
        return consultaMedicamentoRepository.findByConsultaId(consultaId).stream().map(cm ->
                new ConsultaMedicamentoResponse(
                        cm.getConsulta().getId(),
                        cm.getMedicamento().getId(),
                        cm.getCantidad(),
                        cm.getMedicamento().getNombre(),
                        cm.getMedicamento().getDescripcion(),
                        cm.getMedicamento().getPrecio()
                )
        ).toList();
    }
}
