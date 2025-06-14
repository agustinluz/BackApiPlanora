package com.ejemplos.service;

import com.ejemplos.modelo.Imagen;
import com.ejemplos.modelo.ImagenRepository;
import com.ejemplos.DTO.Imagen.ImagenCreateDTO;
import com.ejemplos.DTO.Imagen.ImagenDTO;
import com.ejemplos.DTO.Imagen.ImagenDTOConverter;
import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.EventoRepository;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.UsuarioRepository;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.GrupoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ImagenService {
    
    @Autowired
    private ImagenRepository imagenRepository;
    
    @Autowired
    private ImagenDTOConverter converter;
    
    @Autowired 
    private GrupoRepository grupoRepository;
    
    @Autowired
    private EventoRepository eventoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    public ImagenDTO subirImagen(MultipartFile archivo, ImagenCreateDTO createDTO) throws IOException {
        // Validaciones del archivo
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        
        if (archivo.getSize() > 5 * 1024 * 1024) { // 5MB máximo
            throw new IllegalArgumentException("El archivo es demasiado grande (máximo 5MB)");
        }
        
        // Validar tipo de contenido
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }
        
        // Convertir archivo a Base64
        String base64Data = Base64.getEncoder().encodeToString(archivo.getBytes());
        
        Imagen imagen = new Imagen();
        imagen.setNombre(archivo.getOriginalFilename());
        imagen.setTipoContenido(contentType);
        imagen.setDatos(base64Data);
        imagen.setTamaño(archivo.getSize());
        
        // Asignar relaciones si es necesario
        if (createDTO.getGrupoId() != null) {
            Optional<Grupo> grupoOpt = grupoRepository.findById(createDTO.getGrupoId());
            if (grupoOpt.isPresent()) {
                imagen.setGrupo(grupoOpt.get());
            } else {
                throw new IllegalArgumentException("Grupo no encontrado con ID: " + createDTO.getGrupoId());
            }
        }
        
        if (createDTO.getEventoId() != null) {
            Optional<Evento> eventoOpt = eventoRepository.findById(createDTO.getEventoId());
            if (eventoOpt.isPresent()) {
                imagen.setEvento(eventoOpt.get());
            } else {
                throw new IllegalArgumentException("Evento no encontrado con ID: " + createDTO.getEventoId());
            }
        }
        
        if (createDTO.getUsuarioId() != null) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(createDTO.getUsuarioId());
            if (usuarioOpt.isPresent()) {
                imagen.setUsuario(usuarioOpt.get());
            } else {
                throw new IllegalArgumentException("Usuario no encontrado con ID: " + createDTO.getUsuarioId());
            }
        }
        
        Imagen imagenGuardada = imagenRepository.save(imagen);
        return converter.convertToDTO(imagenGuardada);
    }
    
    public List<ImagenDTO> obtenerImagenesPorGrupoSinDatos(Long grupoId) {
        List<Imagen> imagenes = imagenRepository.findByGrupoIdOrderByFechaCreacionDesc(grupoId);
        return imagenes.stream()
                .map(img -> new ImagenDTO(
                    img.getId(), 
                    img.getNombre(), 
                    img.getTipoContenido(), 
                    img.getTamaño(), 
                    img.getFechaCreacion(),
                    img.getEvento() != null ? img.getEvento().getId() : null,
                    img.getUsuario() != null ? img.getUsuario().getId() : null,
                    img.getGrupo() != null ? img.getGrupo().getId() : null,
                    img.getUsuario() != null ? img.getUsuario().getNombre() : null
                ))
                .collect(Collectors.toList());
    }
    
    public ImagenDTO obtenerImagenCompletaPorId(Long id) {
        Imagen imagen = imagenRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));
        return converter.convertToDTO(imagen); // Este incluye los datos Base64
    }
    
    /**
     * Obtener solo los datos Base64 de una imagen (para endpoints específicos)
     */
    public String obtenerDatosBase64PorId(Long id) {
        Imagen imagen = imagenRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));
        return imagen.getDatos();
    }
    
    /**
     * Obtener todas las imágenes de un evento (sin datos Base64)
     */
    public List<ImagenDTO> obtenerImagenesPorEventoSinDatos(Long eventoId) {
        List<Imagen> imagenes = imagenRepository.findByEventoIdOrderByFechaCreacionDesc(eventoId);
        return imagenes.stream()
                .map(img -> new ImagenDTO(
                    img.getId(), 
                    img.getNombre(), 
                    img.getTipoContenido(), 
                    img.getTamaño(), 
                    img.getFechaCreacion(),
                    img.getEvento() != null ? img.getEvento().getId() : null,
                    img.getUsuario() != null ? img.getUsuario().getId() : null,
                    img.getGrupo() != null ? img.getGrupo().getId() : null,
                    img.getUsuario() != null ? img.getUsuario().getNombre() : null
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener todas las imágenes de un usuario (sin datos Base64)
     */
    public List<ImagenDTO> obtenerImagenesPorUsuarioSinDatos(Long usuarioId) {
        List<Imagen> imagenes = imagenRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        return imagenes.stream()
                .map(img -> new ImagenDTO(
                    img.getId(), 
                    img.getNombre(), 
                    img.getTipoContenido(), 
                    img.getTamaño(), 
                    img.getFechaCreacion(),
                    img.getEvento() != null ? img.getEvento().getId() : null,
                    img.getUsuario() != null ? img.getUsuario().getId() : null,
                    img.getGrupo() != null ? img.getGrupo().getId() : null,
                    img.getUsuario() != null ? img.getUsuario().getNombre() : null
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Elimina una imagen por su ID
     */
    public void eliminarImagen(Long id) {
        Imagen imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));
        
        imagenRepository.delete(imagen);
    }

    /**
     * Elimina una imagen con validación de permisos
     */
    public void eliminarImagenConValidacion(Long id, Long usuarioId) {
        Imagen imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));
        
        // Validación: verificar si el usuario tiene permisos para eliminar
        if (usuarioId != null && imagen.getUsuario() != null && 
            !imagen.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tienes permisos para eliminar esta imagen");
        }
        
        imagenRepository.delete(imagen);
    }

    /**
     * Elimina múltiples imágenes por sus IDs
     */
    public int eliminarImagenes(List<Long> ids) {
        int eliminadas = 0;
        
        for (Long id : ids) {
            try {
                eliminarImagen(id);
                eliminadas++;
            } catch (RuntimeException e) {
                // Log del error si es necesario
                System.err.println("Error eliminando imagen con ID " + id + ": " + e.getMessage());
            }
        }
        
        return eliminadas;
    }
    
    /**
     * Obtener estadísticas de imágenes por grupo
     */
    public long contarImagenesPorGrupo(Long grupoId) {
        return imagenRepository.countByGrupoId(grupoId);
    }
    
    public long contarImagenesPorGrupoYUsuario(Long grupoId, Long usuarioId) {
        return imagenRepository.countByGrupoIdAndUsuarioId(grupoId, usuarioId);
    }
    
    /**
     * Obtener las últimas N imágenes de un grupo
     */
    public List<ImagenDTO> obtenerUltimasImagenesGrupo(Long grupoId, int limite) {
        List<Imagen> imagenes;
        if (limite <= 10) {
            imagenes = imagenRepository.findTop10ByGrupoIdOrderByFechaCreacionDesc(grupoId);
        } else {
            imagenes = imagenRepository.findByGrupoIdOrderByFechaCreacionDesc(grupoId);
        }
        
        return imagenes.stream()
                .limit(limite)
                .map(img -> new ImagenDTO(
                    img.getId(), 
                    img.getNombre(), 
                    img.getTipoContenido(), 
                    img.getTamaño(), 
                    img.getFechaCreacion(),
                    img.getEvento() != null ? img.getEvento().getId() : null,
                    img.getUsuario() != null ? img.getUsuario().getId() : null,
                    img.getGrupo() != null ? img.getGrupo().getId() : null,
                    img.getUsuario() != null ? img.getUsuario().getNombre() : null
                ))
                .collect(Collectors.toList());
    }
}