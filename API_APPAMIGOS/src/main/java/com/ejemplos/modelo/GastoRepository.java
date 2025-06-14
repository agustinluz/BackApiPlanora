package com.ejemplos.modelo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GastoRepository extends JpaRepository<Gasto, Long> {
	List<Gasto> findByGrupoId(Long grupoId);
    @Query("SELECT g FROM Gasto g WHERE g.grupo.id = :grupoId AND g.evento.id = :eventoId")
    List<Gasto> findByGrupoIdAndEventoId(@Param("grupoId") Long grupoId, @Param("eventoId") Long eventoId);

    @Query("SELECT COUNT(g) FROM Gasto g WHERE g.grupo.id = :grupoId AND g.pagadoPor.id = :usuarioId")
    long countByGrupoIdAndPagadoPorId(@Param("grupoId") Long grupoId, @Param("usuarioId") Long usuarioId);
}
