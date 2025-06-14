package com.ejemplos.DTO.Grupo;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.UsuarioGrupo;

@Component
public class GrupoDTOConverter {
    
    public GrupoDTO convertToDTO(Grupo grupo) {
        GrupoDTO dto = new GrupoDTO();
        dto.setId(grupo.getId());
        dto.setNombre(grupo.getNombre());
        dto.setCodigoInvitacion(grupo.getCodigoInvitacion());
        dto.setImagenPerfil(grupo.getImagenPerfil());
        Optional<UsuarioGrupo> adminUG = grupo.getUsuarioGrupos()
                .stream()
                .filter(ug -> "admin".equalsIgnoreCase(ug.getRol()))
                .findFirst();

        adminUG.ifPresent(ug -> dto.setAdminId(ug.getUsuario().getId()));

        return dto;
    }
    
    public Grupo convertToEntity(GrupoCreateDTO createDTO) {
        Grupo grupo = new Grupo();
        grupo.setNombre(createDTO.getNombre());
        grupo.setImagenPerfil(createDTO.getImagenPerfil());
        grupo.setCodigoInvitacion(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return grupo;
    }
}