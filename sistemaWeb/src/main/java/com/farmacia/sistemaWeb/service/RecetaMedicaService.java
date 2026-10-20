package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.Consulta;
import com.farmacia.sistemaWeb.entity.RecetaMedica;
import com.farmacia.sistemaWeb.repository.ConsultaRepository;
import com.farmacia.sistemaWeb.repository.RecetaMedicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RecetaMedicaService {

    @Autowired
    private RecetaMedicaRepository recetaRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    public RecetaMedica generarReceta(String codigoConsulta, RecetaMedica recetaMedica) {
        Optional<Consulta> consultaOpt = consultaRepository.findById(codigoConsulta);
        if (consultaOpt.isPresent()) {
            Consulta consulta = consultaOpt.get();
            // Check if already exists
            Optional<RecetaMedica> existente = recetaRepository.findByConsultaCodigoConsulta(codigoConsulta);
            if (existente.isPresent()) {
                throw new RuntimeException("Ya existe una receta médica para esta consulta.");
            }
            if (recetaMedica.getMedicamentos() == null || recetaMedica.getMedicamentos().isBlank()) {
                throw new RuntimeException("Los medicamentos de la receta son obligatorios.");
            }
            if (recetaMedica.getIndicaciones() == null) {
                recetaMedica.setIndicaciones(""); // columna NOT NULL: sin esto el error era del driver SQL
            }
            recetaMedica.setId(null); // un id enviado por el cliente sobrescribiría otra receta
            recetaMedica.setConsulta(consulta);
            return recetaRepository.save(recetaMedica);
        }
        throw new RuntimeException("Consulta no encontrada");
    }

    public Optional<RecetaMedica> obtenerRecetaPorConsulta(String codigoConsulta) {
        return recetaRepository.findByConsultaCodigoConsulta(codigoConsulta);
    }
}
