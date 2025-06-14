package com.ejemplos.DTO.Invitacion;

import org.springframework.stereotype.Component;

import com.ejemplos.modelo.Invitacion;

@Component
public class InvitacionDTOConverter {
    public InvitacionDTO convertToDTO(Invitacion invitacion) {
        if (invitacion == null) return null;

        InvitacionDTO dto = new InvitacionDTO();
        dto.setId(invitacion.getId());
        dto.setEstado(invitacion.getEstado());
        dto.setFecha(invitacion.getFecha());

        if (invitacion.getGrupo() != null) {
            dto.setGrupoId(invitacion.getGrupo().getId());
            dto.setGrupoNombre(invitacion.getGrupo().getNombre());
        }
        if (invitacion.getUsuarioInvitado() != null) {
            dto.setUsuarioInvitadoId(invitacion.getUsuarioInvitado().getId());
            dto.setUsuarioInvitadoNombre(invitacion.getUsuarioInvitado().getNombre());
        }
        return dto;
    }
}