package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.AsistenciaDiaria;
import com.farmacia.sistemaWeb.entity.Usuario;
import com.farmacia.sistemaWeb.repository.AsistenciaDiariaRepository;
import com.farmacia.sistemaWeb.repository.UsuarioRepository;
import com.farmacia.sistemaWeb.entity.HorarioTrabajador;
import com.farmacia.sistemaWeb.repository.HorarioTrabajadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AsistenciaService {

    @Autowired
    private AsistenciaDiariaRepository asistenciaRepository;

    @Autowired
    private HorarioTrabajadorRepository horarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Métodos para el Administrador
    public List<AsistenciaDiaria> listarTodas() {
        return asistenciaRepository.findAll();
    }

    public List<HorarioTrabajador> listarHorarios() {
        return horarioRepository.findAll();
    }

    public HorarioTrabajador guardarHorario(Long usuarioId, String diaSemana, String horaInicio, String horaFin) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        HorarioTrabajador h = new HorarioTrabajador();
        h.setUsuario(u);
        h.setDiaSemana(java.time.DayOfWeek.valueOf(diaSemana));
        h.setHoraInicio(java.time.LocalTime.parse(horaInicio));
        h.setHoraFin(java.time.LocalTime.parse(horaFin));
        h.setActivo(true);
        return horarioRepository.save(h);
    }

    public void guardarHorariosMultiples(Long usuarioId, List<String> diasSemana, String horaInicio, String horaFin) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        for(String dia : diasSemana) {
            HorarioTrabajador h = new HorarioTrabajador();
            h.setUsuario(u);
            h.setDiaSemana(java.time.DayOfWeek.valueOf(dia));
            h.setHoraInicio(java.time.LocalTime.parse(horaInicio));
            h.setHoraFin(java.time.LocalTime.parse(horaFin));
            h.setActivo(true);
            horarioRepository.save(h);
        }
    }

    public void eliminarHorario(Long id) {
        horarioRepository.deleteById(id);
    }

    // Obtener estado actual del turno
    public AsistenciaDiaria obtenerTurnoActual(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        return asistenciaRepository.findByUsuarioAndFechaAndEstado(usuario, LocalDate.now(), AsistenciaDiaria.EstadoAsistencia.ABIERTO)
                .orElse(null);
    }
    
    // Verificar si el turno está abierto (usado para bloqueos)
    public boolean tieneTurnoAbierto(String emailUsuario) {
        return obtenerTurnoActual(emailUsuario) != null;
    }

    // Abrir turno
    public AsistenciaDiaria abrirTurno(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar si ya tiene un turno abierto hoy
        Optional<AsistenciaDiaria> turnoAbierto = asistenciaRepository.findByUsuarioAndFechaAndEstado(usuario, LocalDate.now(), AsistenciaDiaria.EstadoAsistencia.ABIERTO);
        if (turnoAbierto.isPresent()) {
            throw new RuntimeException("Ya tienes un turno abierto.");
        }

        AsistenciaDiaria asistencia = new AsistenciaDiaria();
        asistencia.setUsuario(usuario);
        asistencia.setFecha(LocalDate.now());
        asistencia.setHoraApertura(LocalDateTime.now());
        asistencia.setEstado(AsistenciaDiaria.EstadoAsistencia.ABIERTO);

        return asistenciaRepository.save(asistencia);
    }

    // Cerrar turno
    public AsistenciaDiaria cerrarTurno(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        AsistenciaDiaria turnoAbierto = asistenciaRepository.findByUsuarioAndFechaAndEstado(usuario, LocalDate.now(), AsistenciaDiaria.EstadoAsistencia.ABIERTO)
                .orElseThrow(() -> new RuntimeException("No tienes un turno abierto para cerrar."));

        turnoAbierto.setHoraCierre(LocalDateTime.now());
        turnoAbierto.setEstado(AsistenciaDiaria.EstadoAsistencia.CERRADO);

        return asistenciaRepository.save(turnoAbierto);
    }
}
