package com.ejemplos.DTO.Usuario;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String email;
    private String fotoPerfil;
    private Long grupoId;
    private List<Long> grupoIds; // En lugar de solo un grupoId
}
