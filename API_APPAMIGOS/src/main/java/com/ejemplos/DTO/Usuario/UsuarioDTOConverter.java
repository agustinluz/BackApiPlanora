package com.ejemplos.DTO.Usuario;

import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.UsuarioGrupo;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UsuarioDTOConverter {
    
    public UsuarioDTO convertToDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setFotoPerfil(usuario.getFotoPerfil());
        
        // Si el usuario tiene grupos asociados, agregar los IDs
        if (usuario.getUsuarioGrupos() != null) {
            List<Long> grupoIds = usuario.getUsuarioGrupos().stream()
                    .map(ug -> ug.getGrupo().getId())
                    .collect(Collectors.toList());
            dto.setGrupoIds(grupoIds);
        }
        
        return dto;
    }
    
    // Nuevo método para convertir UsuarioGrupo a UsuarioDTO con información del rol
    public UsuarioDTO convertFromUsuarioGrupo(UsuarioGrupo usuarioGrupo) {
        if (usuarioGrupo == null || usuarioGrupo.getUsuario() == null) {
            return null;
        }
        
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuarioGrupo.getUsuario().getId());
        dto.setNombre(usuarioGrupo.getUsuario().getNombre());
        dto.setEmail(usuarioGrupo.getUsuario().getEmail());
        dto.setFotoPerfil(usuarioGrupo.getUsuario().getFotoPerfil());
        dto.setGrupoId(usuarioGrupo.getGrupo().getId());
        // El rol se puede obtener del UsuarioGrupoDTO si es necesario
        
        return dto;
    }
    
    public List<UsuarioDTO> convertToDTOList(List<Usuario> usuarios) {
        return usuarios.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Nuevo método para convertir lista de UsuarioGrupo a UsuarioDTO
    public List<UsuarioDTO> convertFromUsuarioGrupoList(List<UsuarioGrupo> usuarioGrupos) {
        return usuarioGrupos.stream()
                .map(this::convertFromUsuarioGrupo)
                .collect(Collectors.toList());
    }
    
    public Usuario convertToEntity(UsuarioDTO dto) {
        if (dto == null) {
            return null;
        }
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setFotoPerfil(dto.getFotoPerfil());
        
        return usuario;
    }
}
