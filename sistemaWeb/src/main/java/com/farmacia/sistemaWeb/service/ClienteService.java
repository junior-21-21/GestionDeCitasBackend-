package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.dto.ClienteDTO;
import com.farmacia.sistemaWeb.dto.ClienteResponseDTO;
import com.farmacia.sistemaWeb.entity.Cliente;
import com.farmacia.sistemaWeb.entity.TelefonoCliente;
import com.farmacia.sistemaWeb.repository.ClienteRepository;
import com.farmacia.sistemaWeb.repository.TelefonoClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private TelefonoClienteRepository telefonoClienteRepository;

    @Transactional
    public Cliente registrarCliente(ClienteDTO dto) {
        if (clienteRepository.existsByDni(dto.getDni())) {
            throw new RuntimeException("El cliente ya está registrado con ese DNI");
        }

        Cliente cliente = new Cliente();
        cliente.setDni(dto.getDni());
        cliente.setNombres(dto.getNombres());
        cliente.setApellidos(dto.getApellidos());
        cliente.setTelefono(dto.getTelefono());
        
        // 1FN: Dirección Atómica
        cliente.setCalle(dto.getCalle());
        cliente.setNumero(dto.getNumero());
        cliente.setDistrito(dto.getDistrito());
        cliente.setProvincia(dto.getProvincia());

        Cliente savedCliente = clienteRepository.save(cliente);

        // 1FN: Teléfonos Múltiples
        if (dto.getTelefonos() != null && !dto.getTelefonos().isEmpty()) {
            for (ClienteDTO.TelefonoDTO telDto : dto.getTelefonos()) {
                TelefonoCliente tel = new TelefonoCliente();
                tel.setNumero(telDto.getNumero());
                tel.setTipo(TelefonoCliente.TipoTelefono.valueOf(telDto.getTipo()));
                tel.setCliente(savedCliente);
                telefonoClienteRepository.save(tel);
            }
        }

        return savedCliente;
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public Cliente buscarClientePorDni(String dni) {
        return clienteRepository.findById(dni)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con DNI: " + dni));
    }

    @Transactional
    public Cliente actualizarCliente(String dni, ClienteDTO dto) {
        Cliente cliente = buscarClientePorDni(dni);
        cliente.setNombres(dto.getNombres());
        cliente.setApellidos(dto.getApellidos());
        cliente.setTelefono(dto.getTelefono());
        
        // 1FN: Dirección Atómica
        cliente.setCalle(dto.getCalle());
        cliente.setNumero(dto.getNumero());
        cliente.setDistrito(dto.getDistrito());
        cliente.setProvincia(dto.getProvincia());
        
        Cliente savedCliente = clienteRepository.save(cliente);

        // 1FN: Actualizar Teléfonos (Recrear para simplificar)
        telefonoClienteRepository.deleteByClienteDni(dni);
        if (dto.getTelefonos() != null && !dto.getTelefonos().isEmpty()) {
            for (ClienteDTO.TelefonoDTO telDto : dto.getTelefonos()) {
                TelefonoCliente tel = new TelefonoCliente();
                tel.setNumero(telDto.getNumero());
                tel.setTipo(TelefonoCliente.TipoTelefono.valueOf(telDto.getTipo()));
                tel.setCliente(savedCliente);
                telefonoClienteRepository.save(tel);
            }
        }

        return savedCliente;
    }

    @Transactional
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