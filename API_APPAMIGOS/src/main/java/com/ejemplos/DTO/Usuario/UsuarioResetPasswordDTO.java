package com.ejemplos.DTO.Usuario;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioResetPasswordDTO {
    private String email;
    private String password;
}