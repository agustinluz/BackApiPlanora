package com.ejemplos.DTO.Nota;

import lombok.Data;

@Data
public class NotaCreateDTO {
    private String titulo;
    private String contenido;
    private Long creadaPorId; // ID del usuario que crea la nota
    private Long eventoId; // ID del evento asociado (opcional)
}