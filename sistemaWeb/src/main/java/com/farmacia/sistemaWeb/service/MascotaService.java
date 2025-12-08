package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.MascotaDTO;
import com.farmacia.sistemaWeb.dto.MascotaResponseDTO;
import com.farmacia.sistemaWeb.entity.Cliente;
import com.farmacia.sistemaWeb.entity.Mascota;
import com.farmacia.sistemaWeb.repository.ClienteRepository;
import com.farmacia.sistemaWeb.repository.MascotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MascotaService {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public MascotaResponseDTO registrarMascota(MascotaDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Mascota mascota = new Mascota();
        mascota.setNombre(dto.getNombre());
        mascota.setEspecie(dto.getEspecie());
        mascota.setRaza(dto.getRaza());
        mascota.setEdad(dto.getEdad());
        mascota.setCliente(cliente);

        return mapToResponseDTO(mascotaRepository.save(mascota));
    }

    public List<MascotaResponseDTO> obtenerMascotasPorCliente(Long clienteId) {
        return mascotaRepository.findByClienteId(clienteId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<MascotaResponseDTO> listarTodas() {
        return mascotaRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private MascotaResponseDTO mapToResponseDTO(Mascota mascota) {
        MascotaResponseDTO dto = new MascotaResponseDTO();
        dto.setId(mascota.getId());
        dto.setNombre(mascota.getNombre());
        dto.setEspecie(mascota.getEspecie());
        dto.setRaza(mascota.getRaza());
        dto.setEdad(mascota.getEdad());
        dto.setClienteId(mascota.getCliente().getId());
        dto.setClienteNombreCompleto(
                mascota.getCliente().getNombres() + " " + mascota.getCliente().getApellidos());
        return dto;
    }

    public void eliminarMascota(Long id) {
        mascotaRepository.deleteById(id);
    }

    public MascotaResponseDTO actualizarMascota(Long id, MascotaDTO dto) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con id: " + id));

        // Obtener el cliente
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Actualizar campos
        mascota.setNombre(dto.getNombre());
        mascota.setEspecie(dto.getEspecie());
        mascota.setRaza(dto.getRaza());
        mascota.setEdad(dto.getEdad());
        mascota.setCliente(cliente); // Asignar la entidad cliente

        Mascota mascotaActualizada = mascotaRepository.save(mascota);

        // Convertir a DTO de respuesta usando mapToResponseDTO
        return mapToResponseDTO(mascotaActualizada);
    }

    public List<MascotaResponseDTO> buscarMascotasPorNombre(String nombre) {
        return mascotaRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<MascotaResponseDTO> buscarMascotasPorDni(String dni) {
        return mascotaRepository.findByClienteDni(dni)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

}
