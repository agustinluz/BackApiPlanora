package com.ejemplos.DTO.Votacion;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

public class VotoDTO {
	private Long id;
    private Long votacionId;
    private Long usuarioId;
    private String opcion;
    private Date fechaVoto;
}
