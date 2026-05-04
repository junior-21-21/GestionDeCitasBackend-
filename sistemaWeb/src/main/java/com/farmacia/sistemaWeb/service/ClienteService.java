package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.ClienteDTO;
import com.farmacia.sistemaWeb.dto.ClienteResponseDTO;
import com.farmacia.sistemaWeb.entity.Cliente;
import com.farmacia.sistemaWeb.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    public Cliente registrarCliente(ClienteDTO dto) {
        if (clienteRepository.existsByDni(dto.getDni())) {
            throw new RuntimeException("El cliente ya está registrado con ese DNI");
        }

        Cliente cliente = new Cliente();
        cliente.setDni(dto.getDni());
        cliente.setNombres(dto.getNombres());
        cliente.setApellidos(dto.getApellidos());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccion(dto.getDireccion());

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public Cliente buscarClientePorDni(String dni) {
        return clienteRepository.findById(dni)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con DNI: " + dni));
    }

    public Cliente actualizarCliente(String dni, ClienteDTO dto) {
        Cliente cliente = buscarClientePorDni(dni);
        cliente.setNombres(dto.getNombres());
        cliente.setApellidos(dto.getApellidos());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccion(dto.getDireccion());
        return clienteRepository.save(cliente);
    }

    public void eliminarCliente(String dni) {
        Cliente cliente = buscarClientePorDni(dni);
        clienteRepository.delete(cliente);
    }

    public ClienteResponseDTO obtenerPorDni(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con DNI: " + dni));

        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setNombres(cliente.getNombres());
        dto.setApellidos(cliente.getApellidos());
        dto.setDni(cliente.getDni());
        return dto;
    }

    public List<ClienteResponseDTO> buscarPorDniParcial(String dni) {
        List<Cliente> clientes = clienteRepository.findByDniContaining(dni);
        return clientes.stream().map(c -> {
            ClienteResponseDTO dto = new ClienteResponseDTO();
            dto.setNombres(c.getNombres());
            dto.setApellidos(c.getApellidos());
            dto.setDni(c.getDni());
            return dto;
        }).collect(Collectors.toList());
    }
}