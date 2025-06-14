package com.ejemplos.DTO.Votacion;

import lombok.Data;

@Data
public class VotoResumenDTO {
    private String opcion;
    private int cantidad;
    private double porcentaje;
}