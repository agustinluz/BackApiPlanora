package com.ejemplos.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventoAsistenteRepository extends JpaRepository<EventoAsistente, Long> {
    List<EventoAsistente> findByEventoId(Long eventoId);
    Optional<EventoAsistente> findByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);
    long countByEventoId(Long eventoId);
    long countByEventoIdAndAsistioTrue(Long eventoId);
}