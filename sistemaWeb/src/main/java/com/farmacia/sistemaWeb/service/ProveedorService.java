package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.Proveedor;
import com.farmacia.sistemaWeb.repository.CompraRepository;
import com.farmacia.sistemaWeb.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private CompraRepository compraRepository;

    public List<Proveedor> listar() {
        return proveedorRepository.findAllByOrderByRazonSocialAsc();
    }

    public Proveedor obtener(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
    }

    @Transactional
    public Proveedor crear(Proveedor proveedor) {
        proveedor.setId(null);
        normalizarYValidar(proveedor);
        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public Proveedor actualizar(Long id, Proveedor datos) {
        Proveedor actual = obtener(id);
        datos.setId(id);
        normalizarYValidar(datos);
        actual.setRuc(datos.getRuc());
        actual.setRazonSocial(datos.getRazonSocial());
        actual.setTelefono(datos.getTelefono());
        actual.setDireccion(datos.getDireccion());
        return proveedorRepository.save(actual);
    }

    @Transactional
    public void eliminar(Long id) {
        Proveedor proveedor = obtener(id);
        if (compraRepository.existsByProveedorId(id)) {
            throw new IllegalArgumentException("No se puede eliminar «" + proveedor.getRazonSocial()
                    + "»: tiene compras registradas");
        }
        proveedorRepository.delete(proveedor);
    }

    private void normalizarYValidar(Proveedor p) {
        String ruc = p.getRuc() == null ? "" : p.getRuc().trim();
        if (!ruc.matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC debe tener exactamente 11 dígitos");
        }
        if (!(ruc.startsWith("10") || ruc.startsWith("15") || ruc.startsWith("17") || ruc.startsWith("20"))) {
            throw new IllegalArgumentException("El RUC debe empezar con 10, 15, 17 o 20");
        }
        p.setRuc(ruc);
        String razon = p.getRazonSocial() == null ? "" : p.getRazonSocial().trim();
        if (razon.isEmpty()) {
            throw new IllegalArgumentException("La razón social es obligatoria");
        }
        if (razon.length() > 150) {
            throw new IllegalArgumentException("La razón social no puede superar 150 caracteres");
        }
        p.setRazonSocial(razon);
        String tel = p.getTelefono() == null ? "" : p.getTelefono().trim();
        if (!tel.isEmpty() && !tel.matches("\\d{7,9}")) {
            throw new IllegalArgumentException("El teléfono debe tener entre 7 y 9 dígitos");
        }
        p.setTelefono(tel.isEmpty() ? null : tel);
        String dir = p.getDireccion() == null ? "" : p.getDireccion().trim();
        if (dir.length() > 255) {
            throw new IllegalArgumentException("La dirección no puede superar 255 caracteres");
        }
        p.setDireccion(dir.isEmpty() ? null : dir);
        proveedorRepository.findByRuc(ruc)
                .filter(otro -> !otro.getId().equals(p.getId()))
                .ifPresent(otro -> {
                    throw new IllegalArgumentException("Ya existe un proveedor con el RUC " + ruc + " (" + otro.getRazonSocial() + ")");
                });
    }
}
