package com.ejemplos.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AsistenciaEventoRepository extends JpaRepository<AsistenciaEvento, Long> {
    @Query("SELECT COUNT(a) FROM AsistenciaEvento a WHERE a.usuario.id = :usuarioId AND a.evento.grupo.id = :grupoId AND a.asistio = true")
    Long countByUsuarioIdAndGrupoId(@Param("usuarioId") Long usuarioId, @Param("grupoId") Long grupoId);
}