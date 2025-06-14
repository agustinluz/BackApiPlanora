package com.ejemplos.DTO.Usuario;

import com.ejemplos.modelo.UsuarioGrupo;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UsuarioGrupoDTOConverter {
    
    public UsuarioGrupoDTO convertToDTO(UsuarioGrupo usuarioGrupo) {
        if (usuarioGrupo == null) {
            return null;
        }
        
        UsuarioGrupoDTO dto = new UsuarioGrupoDTO();
        dto.setUsuarioId(usuarioGrupo.getUsuario().getId());
        dto.setGrupoId(usuarioGrupo.getGrupo().getId());
        dto.setRol(usuarioGrupo.getRol());
        
        // Información adicional del usuario
        dto.setNombreUsuario(usuarioGrupo.getUsuario().getNombre());
        dto.setEmailUsuario(usuarioGrupo.getUsuario().getEmail());
        dto.setFotoPerfil(usuarioGrupo.getUsuario().getFotoPerfil());
        
        // Información adicional del grupo
        dto.setNombreGrupo(usuarioGrupo.getGrupo().getNombre());
        
        return dto;
    }
    
    public List<UsuarioGrupoDTO> convertToDTOList(List<UsuarioGrupo> usuarioGrupos) {
        return usuarioGrupos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public UsuarioGrupo convertToEntity(UsuarioGrupoDTO dto) {
        if (dto == null) {
            return null;
        }
        
        UsuarioGrupo usuarioGrupo = new UsuarioGrupo();
        usuarioGrupo.setRol(dto.getRol());
        // Los objetos Usuario y Grupo se asignarían desde el servicio
        
        return usuarioGrupo;
    }
}
