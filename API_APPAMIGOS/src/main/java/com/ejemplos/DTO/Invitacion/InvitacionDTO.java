package com.ejemplos.DTO.Invitacion;

import java.util.Date;

import lombok.Data;

@Data
public class InvitacionDTO {
    private Long id;
    private Long grupoId;
    private String grupoNombre;
    private Long usuarioInvitadoId;
    private String usuarioInvitadoNombre;
    private String estado;
    private Date fecha;
}