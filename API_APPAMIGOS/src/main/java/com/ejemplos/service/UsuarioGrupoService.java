package com.ejemplos.service;

import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.UsuarioGrupo;
import com.ejemplos.modelo.UsuarioGrupoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioGrupoService {

    @Autowired
    private UsuarioGrupoRepository usuarioGrupoRepository;

    public UsuarioGrupo guardar(UsuarioGrupo usuarioGrupo) {
        return usuarioGrupoRepository.save(usuarioGrupo);
    }

    public List<Usuario> obtenerUsuariosPorGrupoId(Long grupoId) {
        return usuarioGrupoRepository.findByGrupoId(grupoId)
                .stream()
                .map(UsuarioGrupo::getUsuario)
                .collect(Collectors.toList());
    }

    public List<UsuarioGrupo> obtenerPorUsuarioId(Long usuarioId) {
        return usuarioGrupoRepository.findByUsuarioId(usuarioId);
    }
    
    public List<UsuarioGrupo> obtenerPorGrupoId(Long grupoId) {
        return usuarioGrupoRepository.findByGrupoId(grupoId);
    }
    
    public boolean usuarioPerteneceAlGrupo(Long usuarioId, Long grupoId) {
        return usuarioGrupoRepository.existsByUsuarioIdAndGrupoId(usuarioId, grupoId);
    }
    public void eliminarUsuarioDeGrupo(Long usuarioId, Long grupoId) {
        usuarioGrupoRepository.findByUsuarioIdAndGrupoId(usuarioId, grupoId)
                .ifPresent(usuarioGrupoRepository::delete);
    }
    
    
    public Optional<UsuarioGrupo> obtenerPorUsuarioIdYGrupoId(Long usuarioId, Long grupoId) {
        return usuarioGrupoRepository.findByUsuarioIdAndGrupoId(usuarioId, grupoId);
    }
    
    
    public int contarParticipantesPorGrupo(Long grupoId) {
        return usuarioGrupoRepository.countByGrupoId(grupoId);
    }
    
    
    public int contarAdminsPorGrupo(Long grupoId) {
        return usuarioGrupoRepository.countByGrupoIdAndRol(grupoId, "admin");
    }
    
    
}
