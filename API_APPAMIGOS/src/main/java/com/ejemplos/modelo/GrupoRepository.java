package com.ejemplos.modelo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    Optional<Grupo> findByCodigoInvitacion(String codigoInvitacion);
}