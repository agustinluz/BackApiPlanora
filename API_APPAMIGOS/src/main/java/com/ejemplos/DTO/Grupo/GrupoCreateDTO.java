package com.ejemplos.DTO.Grupo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GrupoCreateDTO {
    private String nombre;
    private String imagenPerfil; // URL o base64 de la imagen
}