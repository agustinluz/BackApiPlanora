package com.ejemplos.DTO.Usuario;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioGrupoDTO {
    private Long usuarioId;
    private Long grupoId;
    private String rol; // "admin" o "miembro"
    
    // Información adicional del usuario (opcional)
    private String nombreUsuario;
    private String emailUsuario;
    private String fotoPerfil;
    
    // Información adicional del grupo (opcional)
    private String nombreGrupo;
}