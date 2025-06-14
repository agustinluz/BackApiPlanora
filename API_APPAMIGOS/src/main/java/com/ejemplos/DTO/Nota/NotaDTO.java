// NotaDTO.java
package com.ejemplos.DTO.Nota;

import lombok.Data;
import java.util.Date;

@Data
public class NotaDTO {
    private Long id;
    private String titulo;
    private String contenido;
    private Long grupoId;
    private String grupoNombre;
    private Long creadaPorId;
    private Long eventoId;
    private String creadaPorNombre;
    private Date fechaCreacion;
}