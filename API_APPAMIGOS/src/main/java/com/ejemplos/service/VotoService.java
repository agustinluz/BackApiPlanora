package com.ejemplos.service;

import com.ejemplos.modelo.Voto;
import com.ejemplos.modelo.VotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VotoService {

    @Autowired
    private VotoRepository votoRepository;

    public Voto crear(Voto voto) {
        return votoRepository.save(voto);
    }

    public Optional<Voto> obtenerPorId(Long id) {
        return votoRepository.findById(id);
    }

    public List<Voto> obtenerPorVotacionId(Long votacionId) {
        return votoRepository.findByVotacionId(votacionId);
    }

    public Optional<Voto> obtenerVotoUsuario(Long votacionId, Long usuarioId) {
        return votoRepository.findByVotacionIdAndUsuarioId(votacionId, usuarioId);
    }

    public List<Voto> obtenerPorUsuarioId(Long usuarioId) {
        return votoRepository.findByUsuarioId(usuarioId);
    }

    public void eliminar(Long id) {
        votoRepository.deleteById(id);
    }

    public void eliminarPorVotacionId(Long votacionId) {
        votoRepository.deleteByVotacionId(votacionId);
    }

    public Long contarVotosPorVotacion(Long votacionId) {
        return votoRepository.countByVotacionId(votacionId);
    }
 // En VotoService
    public Optional<Voto> buscarVotoPorUsuarioYVotacion(Long usuarioId, Long votacionId) {
        return votoRepository.findByUsuarioIdAndVotacionId(usuarioId, votacionId);
    }
    
    public long contarVotosPorUsuarioYGrupo(Long usuarioId, Long grupoId) {
        return votoRepository.countByUsuarioIdAndGrupoId(usuarioId, grupoId);
    }
}