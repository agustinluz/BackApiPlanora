package com.ejemplos.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.EventoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    public List<Evento> obtenerPorGrupo(Long grupoId) {
        return eventoRepository.findByGrupoId(grupoId);
    }

    public Evento crear(Evento evento) {
        return eventoRepository.save(evento);
    }
    
    public Optional<Evento> obtenerPorId(Long id) {
        return eventoRepository.findById(id);
    }

    public Evento actualizar(Evento evento) {
        return eventoRepository.save(evento);
    }

    public void eliminar(Long id) {
        eventoRepository.deleteById(id);
    }

    public boolean existePorId(Long id) {
        return eventoRepository.existsById(id);
    }

}
