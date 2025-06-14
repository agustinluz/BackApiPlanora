package com.ejemplos.modelo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitacionRepository extends JpaRepository<Invitacion, Long> {
    List<Invitacion> findByUsuarioInvitadoId(Long usuarioId);
}