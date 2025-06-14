package com.ejemplos.DTO.Imagen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenCreateDTO {
    private Long eventoId;
    private Long usuarioId;
    private Long grupoId;
}