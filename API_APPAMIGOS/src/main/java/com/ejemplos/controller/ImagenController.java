package com.ejemplos.controller;

import com.ejemplos.DTO.Imagen.ImagenCreateDTO;
import com.ejemplos.DTO.Imagen.ImagenDTO;
import com.ejemplos.service.ImagenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    @Autowired
    private ImagenService imagenService;

    /**
     * Subir una nueva imagen - se guarda como Base64 en MySQL
     */
    @PostMapping("/subir")
    public ResponseEntity<?> subirImagen(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "eventoId", required = false) Long eventoId,
            @RequestParam(value = "usuarioId", required = false) Long usuarioId,
            @RequestParam(value = "grupoId", required = false) Long grupoId) {

        try {
            ImagenCreateDTO createDTO = new ImagenCreateDTO(eventoId, usuarioId, grupoId);
            ImagenDTO imagenDTO = imagenService.subirImagen(archivo, createDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Imagen subida correctamente");
            response.put("imagen", imagenDTO);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al subir la imagen: " + e.getMessage()));
        }
    }

    /**
     * Obtener todas las imágenes de un grupo (sin los datos Base64 para optimizar)
     */
    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<?> obtenerImagenesPorGrupo(@PathVariable Long grupoId) {
        try {
            List<ImagenDTO> imagenes = imagenService.obtenerImagenesPorGrupoSinDatos(grupoId);
            return ResponseEntity.ok(Map.of(
                "imagenes", imagenes,
                "total", imagenes.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener imágenes: " + e.getMessage()));
        }
    }

    /**
     * Obtener todas las imágenes de un evento (sin datos Base64)
     */
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<?> obtenerImagenesPorEvento(@PathVariable Long eventoId) {
        try {
            List<ImagenDTO> imagenes = imagenService.obtenerImagenesPorEventoSinDatos(eventoId);
            return ResponseEntity.ok(Map.of(
                "imagenes", imagenes,
                "total", imagenes.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener imágenes: " + e.getMessage()));
        }
    }

    /**
     * Obtener todas las imágenes de un usuario (sin datos Base64)
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerImagenesPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<ImagenDTO> imagenes = imagenService.obtenerImagenesPorUsuarioSinDatos(usuarioId);
            return ResponseEntity.ok(Map.of(
                "imagenes", imagenes,
                "total", imagenes.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener imágenes: " + e.getMessage()));
        }
    }

    /**
     * Obtener imagen completa con datos Base64
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerImagenCompleta(@PathVariable Long id) {
        try {
            ImagenDTO imagen = imagenService.obtenerImagenCompletaPorId(id);
            return ResponseEntity.ok(imagen);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Imagen no encontrada"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener imagen: " + e.getMessage()));
        }
    }

    /**
     * Obtener solo los datos Base64 de una imagen
     */
    @GetMapping("/{id}/datos")
    public ResponseEntity<?> obtenerDatosImagen(@PathVariable Long id) {
        try {
            String datosBase64 = imagenService.obtenerDatosBase64PorId(id);
            return ResponseEntity.ok(Map.of("datos", datosBase64));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Imagen no encontrada"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener datos de imagen: " + e.getMessage()));
        }
    }

    /**
     * Eliminar una imagen
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarImagen(@PathVariable Long id) {
        try {
            imagenService.eliminarImagen(id);
            return ResponseEntity.ok(Map.of("mensaje", "Imagen eliminada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Imagen no encontrada"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar imagen: " + e.getMessage()));
        }
    }


}