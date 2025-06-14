package com.ejemplos.modelo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VotacionRepository extends JpaRepository<Votacion, Long> {
	List<Votacion> findByGrupoId(Long grupoId);
    
    List<Votacion> findByCreadorId(Long creadorId);
    
    List<Votacion> findByGrupoIdAndEstado(Long grupoId, Votacion.EstadoVotacion estado);
    
    List<Votacion> findByEstado(Votacion.EstadoVotacion estado);
    
    long countByGrupoId(Long grupoId);
    
    long countByGrupoIdAndEstado(Long grupoId, Votacion.EstadoVotacion estado);
    
    long countByGrupoIdAndCreadorId(Long grupoId, Long creadorId);
}
