package com.ejemplos.DTO.Evento;

import com.ejemplos.modelo.Evento;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
@Component
public class EventoDTOConverter {
    
	public EventoDTO convertToDTO(Evento evento) {
	    if (evento == null) return null;

	    EventoDTO dto = new EventoDTO();
	    dto.setId(evento.getId());
	    dto.setTitulo(evento.getTitulo());
	    dto.setDescripcion(evento.getDescripcion());
	    dto.setUbicacion(evento.getUbicacion());
	    dto.setFecha(evento.getFecha());

	    if (evento.getGrupo() != null) {
	        dto.setGrupoId(evento.getGrupo().getId());
	        dto.setGrupoNombre(evento.getGrupo().getNombre());
	    }
	    // ← NUEVO:
	    if (evento.getCreador() != null) {
	        dto.setCreadorId(evento.getCreador().getId());
	    }

	    return dto;
	}
    public Evento convertToEntity(EventoCreateDTO dto) {
        if (dto == null) return null;
        
        Evento evento = new Evento();
        evento.setTitulo(dto.getTitulo());
        evento.setDescripcion(dto.getDescripcion());
        evento.setUbicacion(dto.getUbicacion());
        
        // Conversión opcional: Si el DTO trae una fecha en UTC, la convierte a la zona local.
        if (dto.getFecha() != null) {
            // Asume que dto.getFecha() está en UTC, cámbialo según tu caso
            LocalDateTime localDateTime = Instant.ofEpochMilli(dto.getFecha().getTime())
                .atZone(ZoneId.systemDefault()).toLocalDateTime(); 
            // O simplemente asigna la fecha si ya viene en la zona correcta.
            evento.setFecha(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        return evento;
    }
}