package com.ejemplos.DTO.Votacion;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class VotacionDTO {
    private Long id;
    private String pregunta; // Usado por el frontend
    private String descripcion; // Agregado para mostrar descripción
    private List<String> opciones;
    private Date fechaCreacion;
    private Date fechaLimite; // Usado por el frontend para fechaCierre
    private Long grupoId;
    private String grupoNombre;
    private Long creadaPorId; // Usado por el frontend para creadorId
    private String creadaPorNombre; // Usado por el frontend para creadorNombre
    private String estado; // Agregado para manejar ACTIVA/CERRADA
    private int totalVotos;
    private List<VotoResumenDTO> resumenVotos;
}