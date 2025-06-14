package com.ejemplos.DTO.Usuario;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioUpdateDTO {
	private String nombre;
    private String email;
    private String fotoPerfil;
    private String password;
    private String currentPassword; // Para validar la contraseña actual antes de cambiarla
}
