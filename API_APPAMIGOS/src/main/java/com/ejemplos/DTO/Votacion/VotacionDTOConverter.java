package com.ejemplos.DTO.Votacion;

import com.ejemplos.modelo.Votacion;
import com.ejemplos.modelo.Voto;
import com.ejemplos.modelo.VotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class VotacionDTOConverter {

    @Autowired
    private VotoRepository votoRepository;

    public VotacionDTO convertToDTO(Votacion votacion) {
        if (votacion == null) {
            return null;
        }

        VotacionDTO dto = new VotacionDTO();
        dto.setId(votacion.getId());
        dto.setPregunta(votacion.getTitulo()); // Mapeo titulo -> pregunta
        dto.setDescripcion(votacion.getDescripcion());
        dto.setOpciones(votacion.getOpciones());
        dto.setFechaCreacion(votacion.getFechaCreacion());
        dto.setFechaLimite(votacion.getFechaCierre()); // Mapeo fechaCierre -> fechaLimite
        dto.setEstado(votacion.getEstado().name()); // Convertir enum a String

        // Información del grupo
        if (votacion.getGrupo() != null) {
            dto.setGrupoId(votacion.getGrupo().getId());
            dto.setGrupoNombre(votacion.getGrupo().getNombre());
        }

        // Información del creador
        if (votacion.getCreador() != null) {
            dto.setCreadaPorId(votacion.getCreador().getId());
            dto.setCreadaPorNombre(votacion.getCreador().getNombre());
        }

        // Obtener y procesar votos
        List<Voto> votos = votoRepository.findByVotacionId(votacion.getId());
        dto.setTotalVotos(votos.size());

        // Crear resumen de votos por opción
        List<VotoResumenDTO> resumenVotos = crearResumenVotos(votacion.getOpciones(), votos);
        dto.setResumenVotos(resumenVotos);

        return dto;
    }

    public Votacion convertToEntity(VotacionCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        Votacion votacion = new Votacion();
        votacion.setTitulo(dto.getPregunta()); // Mapeo pregunta -> titulo
        votacion.setDescripcion(dto.getDescripcion());
        votacion.setOpciones(dto.getOpciones());
        votacion.setFechaCierre(dto.getFechaLimite()); // Mapeo fechaLimite -> fechaCierre
        votacion.setEstado(Votacion.EstadoVotacion.ACTIVA); // Por defecto ACTIVA

        return votacion;
    }

    private List<VotoResumenDTO> crearResumenVotos(List<String> opciones, List<Voto> votos) {
        List<VotoResumenDTO> resumen = new ArrayList<>();

        // Contar votos por opción
        Map<String, Long> conteoVotos = votos.stream()
            .collect(Collectors.groupingBy(Voto::getOpcion, Collectors.counting()));

        // Calcular total de votos para porcentajes
        int totalVotos = votos.size();

        // Crear resumen para cada opción
        for (String opcion : opciones) {
            VotoResumenDTO votoResumen = new VotoResumenDTO();
            votoResumen.setOpcion(opcion);
            
            long cantidadVotos = conteoVotos.getOrDefault(opcion, 0L);
            votoResumen.setCantidad((int) cantidadVotos);
            
            // Calcular porcentaje
            double porcentaje = totalVotos > 0 ? (cantidadVotos * 100.0) / totalVotos : 0.0;
            votoResumen.setPorcentaje(Math.round(porcentaje * 100.0) / 100.0); // Redondear a 2 decimales
            
            resumen.add(votoResumen);
        }

        return resumen;
    }

    public List<VotacionDTO> convertToDTOList(List<Votacion> votaciones) {
        if (votaciones == null) {
            return null;
        }

        return votaciones.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
}