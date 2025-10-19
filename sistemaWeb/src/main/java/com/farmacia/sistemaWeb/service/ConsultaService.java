package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.ConsultaDTO;
import com.farmacia.sistemaWeb.entity.Consulta;
import com.farmacia.sistemaWeb.entity.Mascota;
import com.farmacia.sistemaWeb.entity.Veterinario;
import com.farmacia.sistemaWeb.repository.ConsultaRepository;
import com.farmacia.sistemaWeb.repository.MascotaRepository;
import com.farmacia.sistemaWeb.repository.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    // ✅ Registrar una consulta
    public Consulta registrarConsulta(ConsultaDTO dto) {
        Mascota mascota = mascotaRepository.findById(dto.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));

        Veterinario veterinario = veterinarioRepository.findById(dto.getVeterinarioId())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        Consulta consulta = new Consulta();
        consulta.setFecha(dto.getFecha());
        consulta.setMotivo(dto.getMotivo());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        consulta.setMascota(mascota);
        consulta.setVeterinario(veterinario);

        return consultaRepository.save(consulta);
    }

    // ✅ Listar todas las consultas
    public List<Consulta> listarConsultas() {
        return consultaRepository.findAll();
    }

    // ✅ Buscar consulta por ID
    public Consulta buscarPorId(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta no encontrada"));
    }

    // ✅ Buscar consultas por DNI del cliente (cliente -> mascotas -> consultas)
    public List<Consulta> buscarConsultasPorDniCliente(String dni) {
        return consultaRepository.findByMascotaClienteDni(dni);
    }

    // (Opcional) Actualizar una consulta
    public Consulta actualizarConsulta(Long id, ConsultaDTO dto) {
        Consulta consulta = buscarPorId(id);

        Mascota mascota = mascotaRepository.findById(dto.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));

        Veterinario veterinario = veterinarioRepository.findById(dto.getVeterinarioId())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        consulta.setFecha(dto.getFecha());
        consulta.setMotivo(dto.getMotivo());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        consulta.setMascota(mascota);
        consulta.setVeterinario(veterinario);

        return consultaRepository.save(consulta);
    }

    // (Opcional) Eliminar una consulta
    public void eliminarConsulta(Long id) {
        Consulta consulta = buscarPorId(id);
        consultaRepository.delete(consulta);
    }
}
