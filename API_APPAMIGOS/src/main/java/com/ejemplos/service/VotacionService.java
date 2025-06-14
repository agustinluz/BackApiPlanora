package com.ejemplos.service;

import com.ejemplos.modelo.Votacion;
import com.ejemplos.modelo.VotacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class VotacionService {
    
    @Autowired
    private VotacionRepository votacionRepository;

    public Votacion crear(Votacion votacion) {
        return votacionRepository.save(votacion);
    }

    public List<Votacion> obtenerPorGrupo(Long grupoId) {
        return votacionRepository.findByGrupoId(grupoId);
    }

    public List<Votacion> obtenerTodas() {
        return votacionRepository.findAll();
    }

    public Optional<Votacion> obtenerPorId(Long id) {
        Optional<Votacion> votacionOpt = votacionRepository.findById(id);
        
        // Verificar si necesita cerrarse automáticamente
        if (votacionOpt.isPresent()) {
            Votacion votacion = votacionOpt.get();
            if (votacion.getFechaCierre() != null && 
                new Date().after(votacion.getFechaCierre()) && 
                Votacion.EstadoVotacion.ACTIVA.equals(votacion.getEstado())) {
                
                votacion.setEstado(Votacion.EstadoVotacion.CERRADA);
                votacion = votacionRepository.save(votacion);
                return Optional.of(votacion);
            }
        }
        
        return votacionOpt;
    }

    public List<Votacion> obtenerPorCreadorId(Long creadorId) {
        return votacionRepository.findByCreadorId(creadorId);
    }

    public List<Votacion> obtenerActivasPorGrupoId(Long grupoId) {
        return votacionRepository.findByGrupoIdAndEstado(grupoId, Votacion.EstadoVotacion.ACTIVA);
    }

    // NUEVO: Método para obtener todas las votaciones activas
    public List<Votacion> obtenerVotacionesActivas() {
        return votacionRepository.findByEstado(Votacion.EstadoVotacion.ACTIVA);
    }

    // NUEVO: Método para cerrar votaciones expiradas
    public int cerrarVotacionesExpiradas() {
        List<Votacion> votacionesActivas = obtenerVotacionesActivas();
        Date ahora = new Date();
        int cerradas = 0;
        
        for (Votacion votacion : votacionesActivas) {
            if (votacion.getFechaCierre() != null && ahora.after(votacion.getFechaCierre())) {
                votacion.setEstado(Votacion.EstadoVotacion.CERRADA);
                votacionRepository.save(votacion);
                cerradas++;
            }
        }
        
        return cerradas;
    }

    public Votacion actualizar(Votacion votacion) {
        return votacionRepository.save(votacion);
    }

    public void eliminar(Long id) {
        votacionRepository.deleteById(id);
    }

    public boolean existePorId(Long id) {
        return votacionRepository.existsById(id);
    }

    public long contarPorGrupoId(Long grupoId) {
        return votacionRepository.countByGrupoId(grupoId);
    }

    public long contarActivasPorGrupoId(Long grupoId) {
        return votacionRepository.countByGrupoIdAndEstado(grupoId, Votacion.EstadoVotacion.ACTIVA);
    }
    
    public long contarPorGrupoYUsuario(Long grupoId, Long usuarioId) {
        return votacionRepository.countByGrupoIdAndCreadorId(grupoId, usuarioId);
    }
}