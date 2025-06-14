package com.ejemplos.DTO.Grupo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GrupoDTO {
    private Long id;
    private String nombre;
    private String codigoInvitacion;
    private String imagenPerfil;
    private Long adminId; // ID del usuario administrador
}