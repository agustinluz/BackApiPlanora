package com.ejemplos.modelo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByGrupoId(Long grupoId);
}

