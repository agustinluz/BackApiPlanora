package com.ejemplos.modelo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public interface VotoRepository extends JpaRepository<Voto, Long> {
    
    List<Voto> findByVotacionId(Long votacionId);
    
    Optional<Voto> findByVotacionIdAndUsuarioId(Long votacionId, Long usuarioId);
    
    List<Voto> findByUsuarioId(Long usuarioId);
    
    Optional<Voto> findByUsuarioIdAndVotacionId(Long usuarioId, Long votacionId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Voto v WHERE v.votacion.id = :votacionId")
    void deleteByVotacionId(@Param("votacionId") Long votacionId);
    
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.votacion.id = :votacionId")
    Long countByVotacionId(@Param("votacionId") Long votacionId);

    @Query("SELECT v.opcion, COUNT(v) FROM Voto v WHERE v.votacion.id = :votacionId GROUP BY v.opcion")
    List<Object[]> countVotesByOption(@Param("votacionId") Long votacionId);

    @Query("SELECT COUNT(v) FROM Voto v WHERE v.usuario.id = :usuarioId AND v.votacion.grupo.id = :grupoId")
    long countByUsuarioIdAndGrupoId(@Param("usuarioId") Long usuarioId, @Param("grupoId") Long grupoId);
}