package com.ejemplos.DTO.Imagen;

import com.ejemplos.modelo.Imagen;
import org.springframework.stereotype.Component;

@Component
public class ImagenDTOConverter {
    
    /**
     * Convierte una entidad Imagen a ImagenDTO con todos los datos (incluye Base64)
     */
    public ImagenDTO convertToDTO(Imagen imagen) {
        if (imagen == null) {
            return null;
        }
        
        ImagenDTO dto = new ImagenDTO();
        dto.setId(imagen.getId());
        dto.setNombre(imagen.getNombre());
        dto.setTipoContenido(imagen.getTipoContenido());
        dto.setDatos(imagen.getDatos()); // Incluye los datos Base64
        dto.setTamaño(imagen.getTamaño());
        dto.setFechaCreacion(imagen.getFechaCreacion());
        dto.setEventoId(imagen.getEvento() != null ? imagen.getEvento().getId() : null);
        dto.setUsuarioId(imagen.getUsuario() != null ? imagen.getUsuario().getId() : null);
        dto.setGrupoId(imagen.getGrupo() != null ? imagen.getGrupo().getId() : null);
        dto.setNombreUsuario(imagen.getUsuario() != null ? imagen.getUsuario().getNombre() : null);
        
        return dto;
    }
    
    /**
     * Convierte una entidad Imagen a ImagenDTO sin datos Base64 (para listados)
     */
    public ImagenDTO convertToDTOSinDatos(Imagen imagen) {
        if (imagen == null) {
            return null;
        }
        
        return new ImagenDTO(
            imagen.getId(),
            imagen.getNombre(),
            imagen.getTipoContenido(),
            imagen.getTamaño(),
            imagen.getFechaCreacion(),
            imagen.getEvento() != null ? imagen.getEvento().getId() : null,
            imagen.getUsuario() != null ? imagen.getUsuario().getId() : null,
            imagen.getGrupo() != null ? imagen.getGrupo().getId() : null,
            imagen.getUsuario() != null ? imagen.getUsuario().getNombre() : null
        );
    }
    
    /**
     * Convierte una entidad Imagen a una versión minimalista del DTO
     */
    public ImagenDTO convertToMinimalDTO(Imagen imagen) {
        if (imagen == null) {
            return null;
        }
        
        ImagenDTO dto = new ImagenDTO();
        dto.setId(imagen.getId());
        dto.setNombre(imagen.getNombre());
        dto.setTipoContenido(imagen.getTipoContenido());
        dto.setFechaCreacion(imagen.getFechaCreacion());
        
        return dto;
    }
}