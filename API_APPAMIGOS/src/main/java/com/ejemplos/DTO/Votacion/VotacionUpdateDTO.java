package com.ejemplos.DTO.Votacion;

import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class VotacionUpdateDTO {
	 private String titulo;
	 private String descripcion;
	 private List<String> opciones;
}
