package com.ejemplos.DTO.Nota;

import com.ejemplos.modelo.Nota;
import org.springframework.stereotype.Component;

@Component
public class NotaDTOConverter {
    
    public NotaDTO convertToDTO(Nota nota) {
        if (nota == null) return null;
        
        NotaDTO dto = new NotaDTO();
        dto.setId(nota.getId());
        dto.setTitulo(nota.getTitulo());
        dto.setContenido(nota.getContenido());
        dto.setFechaCreacion(nota.getFechaCreacion()); 
        
        if (nota.getGrupo() != null) {
            dto.setGrupoId(nota.getGrupo().getId());
            dto.setGrupoNombre(nota.getGrupo().getNombre());
        }
        if (nota.getEvento() != null) {
            dto.setEventoId(nota.getEvento().getId());
        }
        if (nota.getUsuario() != null) {
            dto.setCreadaPorId(nota.getUsuario().getId());
            dto.setCreadaPorNombre(nota.getUsuario().getNombre());
        }
        
        return dto;
    }
    
    public Nota convertToEntity(NotaCreateDTO dto) {
        if (dto == null) return null;
        
        Nota nota = new Nota();
        nota.setTitulo(dto.getTitulo());
        nota.setContenido(dto.getContenido());
        
        return nota;
    }
}