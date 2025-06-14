package com.ejemplos.DTO.Usuario;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioCreateDTO {
    private String nombre;
    private String email;
    private String password;
}
