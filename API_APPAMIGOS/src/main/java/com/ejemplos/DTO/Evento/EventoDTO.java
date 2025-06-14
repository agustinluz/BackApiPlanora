package com.ejemplos.DTO.Evento;

import lombok.Data;
import java.util.Date;

@Data
public class EventoDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String ubicacion;
    private Date fecha;
    private Long grupoId;
    private String grupoNombre;

    // ← NUEVO campo
    private Long creadorId;
}

