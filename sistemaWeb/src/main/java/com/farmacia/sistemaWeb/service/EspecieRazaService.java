package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.Especie;
import com.farmacia.sistemaWeb.entity.Raza;
import com.farmacia.sistemaWeb.repository.EspecieRepository;
import com.farmacia.sistemaWeb.repository.RazaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EspecieRazaService {

    @Autowired
    private EspecieRepository especieRepository;

    @Autowired
    private RazaRepository razaRepository;

    // === ESPECIES ===

    public List<Especie> listarEspecies() {
        return especieRepository.findAll();
    }

    public Especie crearEspecie(String nombre) {
        if (especieRepository.existsByNombreIgnoreCase(nombre.trim())) {
            throw new RuntimeException("Ya existe una especie con el nombre: " + nombre);
        }
        Especie especie = new Especie();
        especie.setNombre(nombre.trim());
        return especieRepository.save(especie);
    }

    public Especie obtenerEspeciePorId(Long id) {
        return especieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especie no encontrada con ID: " + id));
    }

    // === RAZAS ===

    public List<Raza> listarRazasPorEspecie(Long especieId) {
        return razaRepository.findByEspecieId(especieId);
    }

    public Raza crearRaza(Long especieId, String nombre) {
        Especie especie = obtenerEspeciePorId(especieId);
        if (razaRepository.existsByNombreIgnoreCaseAndEspecieId(nombre.trim(), especieId)) {
            throw new RuntimeException("Ya existe la raza '" + nombre + "' para la especie '" + especie.getNombre() + "'");
        }
        Raza raza = new Raza();
        raza.setNombre(nombre.trim());
        raza.setEspecie(especie);
        return razaRepository.save(raza);
    }

    public Raza obtenerRazaPorId(Long id) {
        return razaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Raza no encontrada con ID: " + id));
    }

    public List<Raza> listarTodasLasRazas() {
        return razaRepository.findAll();
    }

    /**
     * Busca o crea una especie y raza. Usado por el DataLoader para migración de datos.
     */
    public Raza obtenerOCrearRaza(String especieNombre, String razaNombre) {
        Especie especie = especieRepository.findByNombreIgnoreCase(especieNombre.trim())
                .orElseGet(() -> {
                    Especie nueva = new Especie();
                    nueva.setNombre(especieNombre.trim());
                    return especieRepository.save(nueva);
                });

        return razaRepository.findByNombreIgnoreCaseAndEspecieId(razaNombre.trim(), especie.getId())
                .orElseGet(() -> {
                    Raza nueva = new Raza();
                    nueva.setNombre(razaNombre.trim());
                    nueva.setEspecie(especie);
                    return razaRepository.save(nueva);
                });
    }
}
