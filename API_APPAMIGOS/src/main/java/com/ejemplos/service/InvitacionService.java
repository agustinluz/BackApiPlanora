 package com.ejemplos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Invitacion;
import com.ejemplos.modelo.InvitacionRepository;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.UsuarioGrupo;
import com.ejemplos.modelo.UsuarioGrupoRepository;

@Service
public class InvitacionService {

    @Autowired
    private InvitacionRepository invitacionRepository;

    @Autowired
    private UsuarioGrupoRepository usuarioGrupoRepository;

    public Invitacion crearInvitacion(Grupo grupo, Usuario usuario) {
        Invitacion invitacion = new Invitacion();
        invitacion.setGrupo(grupo);
        invitacion.setUsuarioInvitado(usuario);
        invitacion.setEstado("PENDIENTE");
        return invitacionRepository.save(invitacion);
    }

    public List<Invitacion> obtenerPorUsuario(Long usuarioId) {
        return invitacionRepository.findByUsuarioInvitadoId(usuarioId);
    }

    public Optional<Invitacion> obtenerPorId(Long id) {
        return invitacionRepository.findById(id);
    }

    @Transactional
    public boolean aceptarInvitacion(Long id) {
        Optional<Invitacion> invitacionOpt = invitacionRepository.findById(id);
        if (invitacionOpt.isEmpty()) {
            return false;
        }
        Invitacion invitacion = invitacionOpt.get();
        if (!"PENDIENTE".equalsIgnoreCase(invitacion.getEstado())) {
            return false;
        }
        invitacion.setEstado("ACEPTADA");
        invitacionRepository.save(invitacion);

        UsuarioGrupo ug = new UsuarioGrupo();
        ug.setGrupo(invitacion.getGrupo());
        ug.setUsuario(invitacion.getUsuarioInvitado());
        ug.setRol("miembro");
        usuarioGrupoRepository.save(ug);
        return true;
    }

    public boolean rechazarInvitacion(Long id) {
        Optional<Invitacion> invitacionOpt = invitacionRepository.findById(id);
        if (invitacionOpt.isEmpty()) {
            return false;
        }
        Invitacion invitacion = invitacionOpt.get();
        if (!"PENDIENTE".equalsIgnoreCase(invitacion.getEstado())) {
            return false;
        }
        invitacion.setEstado("RECHAZADA");
        invitacionRepository.save(invitacion);
        return true;
    }
}