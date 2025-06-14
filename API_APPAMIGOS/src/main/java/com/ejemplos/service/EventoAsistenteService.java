package com.ejemplos.service;

import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.EventoAsistente;
import com.ejemplos.modelo.EventoAsistenteRepository;
import com.ejemplos.modelo.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EventoAsistenteService {

    @Autowired
    private EventoAsistenteRepository repository;

    public EventoAsistente marcarAsistencia(Evento evento, Usuario usuario, boolean asistio) {
        EventoAsistente ea = repository.findByEventoIdAndUsuarioId(evento.getId(), usuario.getId())
                .orElse(new EventoAsistente());
        ea.setEvento(evento);
        ea.setUsuario(usuario);
        ea.setAsistio(asistio);
        return repository.save(ea);
    }

    public Optional<EventoAsistente> obtenerPorEventoYUsuario(Long eventoId, Long usuarioId) {
        return repository.findByEventoIdAndUsuarioId(eventoId, usuarioId);
    }

    public long contarInvitados(Long eventoId) {
        return repository.countByEventoId(eventoId);
    }

    public long contarAsistentes(Long eventoId) {
        return repository.countByEventoIdAndAsistioTrue(eventoId);
    }
    public java.util.List<EventoAsistente> obtenerPorEvento(Long eventoId) {
        return repository.findByEventoId(eventoId);
    }
}