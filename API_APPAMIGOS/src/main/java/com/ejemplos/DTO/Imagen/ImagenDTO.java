package com.ejemplos.DTO.Imagen;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenDTO {
    private Long id;
    private String nombre;
    private String tipoContenido;
    private String datos; // Base64 string - ahora incluimos los datos
    private Long tamaño;
    private LocalDateTime fechaCreacion;
    private Long eventoId;
    private Long usuarioId;
    private Long grupoId;
    private String nombreUsuario;
    
    // Constructor sin datos para listados (cuando no necesites la imagen completa)
    public ImagenDTO(Long id, String nombre, String tipoContenido, Long tamaño, 
                     LocalDateTime fechaCreacion, Long eventoId, Long usuarioId, 
                     Long grupoId, String nombreUsuario) {
        this.id = id;
        this.nombre = nombre;
        this.tipoContenido = tipoContenido;
        this.tamaño = tamaño;
        this.fechaCreacion = fechaCreacion;
        this.eventoId = eventoId;
        this.usuarioId = usuarioId;
        this.grupoId = grupoId;
        this.nombreUsuario = nombreUsuario;
        // datos se queda null para listados
    }
}