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

    @org.springframework.transaction.annotation.Transactional
    public void guardarHorariosMultiples(Long usuarioId, List<String> diasSemana, String horaInicio, String horaFin) {
        if (usuarioId == null) {
            throw new RuntimeException("El usuario es obligatorio");
        }
        if (diasSemana == null || diasSemana.isEmpty()) {
            throw new RuntimeException("Seleccione al menos un día de la semana");
        }
        java.time.LocalTime inicio;
        java.time.LocalTime fin;
        try {
            inicio = java.time.LocalTime.parse(horaInicio);
            fin = java.time.LocalTime.parse(horaFin);
        } catch (RuntimeException e) {
            throw new RuntimeException("Formato de hora inválido (use HH:mm)");
        }
        if (!inicio.isBefore(fin)) {
            throw new RuntimeException("La hora de inicio debe ser anterior a la hora de fin");
        }
        java.util.List<java.time.DayOfWeek> dias = new java.util.ArrayList<>();
        for (String dia : diasSemana) {
            try {
                dias.add(java.time.DayOfWeek.valueOf(dia.trim().toUpperCase()));
            } catch (RuntimeException e) {
                throw new RuntimeException("Día de la semana inválido: " + dia);
            }
        }

        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Guardar un dia que ya tenia horario lo reemplaza (antes se duplicaba y el PDF mostraba solo uno).
        List<HorarioTrabajador> existentes = horarioRepository.findByUsuario(u);
        for (HorarioTrabajador previo : existentes) {
            if (dias.contains(previo.getDiaSemana())) {
                horarioRepository.delete(previo);
            }
        }

        for (java.time.DayOfWeek dia : new java.util.LinkedHashSet<>(dias)) {
            HorarioTrabajador h = new HorarioTrabajador();
            h.setUsuario(u);
            h.setDiaSemana(dia);
            h.setHoraInicio(inicio);
            h.setHoraFin(fin);
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
                
        return asistenciaRepository.findFirstByUsuarioAndEstadoOrderByHoraAperturaDesc(usuario, AsistenciaDiaria.EstadoAsistencia.ABIERTO)
                .orElse(null);
    }
    
    // Verificar si el turno está abierto (usado para bloqueos)
    public boolean tieneTurnoAbierto(String emailUsuario) {
        return obtenerTurnoActual(emailUsuario) != null;
    }

    // Abrir turno
    @org.springframework.transaction.annotation.Transactional
    public AsistenciaDiaria abrirTurno(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar si ya tiene un turno abierto hoy
        // Cualquier turno abierto cuenta, aunque sea de ayer: antes uno sin cerrar de ayer permitia abrir otro.
        Optional<AsistenciaDiaria> turnoAbierto = asistenciaRepository.findFirstByUsuarioAndEstadoOrderByHoraAperturaDesc(usuario, AsistenciaDiaria.EstadoAsistencia.ABIERTO);
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
    @org.springframework.transaction.annotation.Transactional
    public AsistenciaDiaria cerrarTurno(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Un turno abierto a las 22:00 y cerrado pasada la medianoche ya no queda abierto para siempre.
        AsistenciaDiaria turnoAbierto = asistenciaRepository.findFirstByUsuarioAndEstadoOrderByHoraAperturaDesc(usuario, AsistenciaDiaria.EstadoAsistencia.ABIERTO)
                .orElseThrow(() -> new RuntimeException("No tienes un turno abierto para cerrar."));

        turnoAbierto.setHoraCierre(LocalDateTime.now());
        turnoAbierto.setEstado(AsistenciaDiaria.EstadoAsistencia.CERRADO);

        return asistenciaRepository.save(turnoAbierto);
    }
}
