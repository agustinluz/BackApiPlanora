package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.Nota.NotaCreateDTO;
import com.ejemplos.DTO.Nota.NotaDTO;
import com.ejemplos.DTO.Nota.NotaDTOConverter;
import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Nota;
import com.ejemplos.modelo.NotaRepository;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.security.JwtUtil;
import com.ejemplos.service.EventoService;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.NotaService;
import com.ejemplos.service.UsuarioGrupoService;
import com.ejemplos.service.UsuarioService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nota")

public class NotaController {

    @Autowired
    private NotaService notaService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private NotaRepository notaRepository;

    @Autowired
    private NotaDTOConverter notaDTOConverter;
    
    @Autowired
    private EventoService eventoService;

    @Autowired
    private UsuarioGrupoService usuarioGrupoService;


    @Autowired
    private JwtUtil jwtUtil;

    
    // Crear nota en un grupo
    // Crear nota en un grupo
    @PostMapping("/{grupoId}/crear")
    public ResponseEntity<NotaDTO> crearNota(
            @PathVariable Long grupoId,
            @RequestBody NotaCreateDTO notaDTO,
            @RequestHeader("Authorization") String token) {
        try {
            // Validar que el grupo existe
            Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
            if (grupo == null) {
                return ResponseEntity.notFound().build();
            }

            // Extraer y validar usuario del token
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Verificar que el usuario pertenece al grupo
            if (!usuarioGrupoService.usuarioPerteneceAlGrupo(usuario.getId(), grupoId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Crear la nota
            Nota nota = notaDTOConverter.convertToEntity(notaDTO);
            nota.setGrupo(grupo);
            nota.setUsuario(usuario);

            if (notaDTO.getEventoId() != null) {
                Evento evento = eventoService.obtenerPorId(notaDTO.getEventoId()).orElse(null);
                if (evento == null || !evento.getGrupo().getId().equals(grupoId)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                nota.setEvento(evento);
            }

            Nota guardada = notaService.crear(nota);
            NotaDTO response = notaDTOConverter.convertToDTO(guardada);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    
    @GetMapping("/{grupoId}/notas")
    public ResponseEntity<List<NotaDTO>> listarNotasGrupo(@PathVariable Long grupoId,
                                                         @RequestParam(required = false) Long eventoId) {
        try {
            List<Nota> notas;
            if (eventoId != null) {
                notas = notaRepository.findByGrupoIdAndEventoId(grupoId, eventoId);
            } else {
                notas = notaRepository.findByGrupoId(grupoId);
            }
            List<NotaDTO> notasDTO = notas.stream()
                    .map(notaDTOConverter::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(notasDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener nota específica por ID
    @GetMapping("/{notaId}")
    public ResponseEntity<NotaDTO> obtenerNota(@PathVariable Long notaId) {
        return notaService.obtenerPorId(notaId)
                .map(notaDTOConverter::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Actualizar nota existente
    @PutMapping("/{notaId}")
    public ResponseEntity<NotaDTO> actualizarNota(
            @PathVariable Long notaId,
            @RequestBody NotaCreateDTO notaDTO,
            @RequestHeader("Authorization") String token) {
        try {
            // Validar token y usuario
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Verificar que la nota existe
            Nota notaExistente = notaService.obtenerPorId(notaId).orElse(null);
            if (notaExistente == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar que el usuario es el propietario de la nota
            if (!notaExistente.getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            System.out.println("ACTUALIZANDO NOTA ID " + notaId);
            System.out.println("Título recibido: " + notaDTO.getTitulo());
            System.out.println("Contenido recibido: " + notaDTO.getContenido());


            // Actualizar los campos de la nota
            notaExistente.setTitulo(notaDTO.getTitulo());
            notaExistente.setContenido(notaDTO.getContenido());

            Nota notaActualizada = notaService.actualizar(notaExistente);
            NotaDTO response = notaDTOConverter.convertToDTO(notaActualizada);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Eliminar nota
    @DeleteMapping("/{notaId}")
    public ResponseEntity<Void> eliminarNota(
            @PathVariable Long notaId,
            @RequestHeader("Authorization") String token) {
        try {
            // Validar token y usuario
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Verificar que la nota existe
            Nota notaExistente = notaService.obtenerPorId(notaId).orElse(null);
            if (notaExistente == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar que el usuario es el propietario de la nota
            if (!notaExistente.getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Eliminar la nota
            notaService.eliminar(notaId);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener todas las notas de un usuario específico
    @GetMapping("/usuario")
    public ResponseEntity<List<NotaDTO>> obtenerNotasUsuario(
            @RequestHeader("Authorization") String token) {
        try {
            // Validar token y usuario
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<Nota> notas = notaRepository.findByUsuarioId(usuario.getId());
            List<NotaDTO> notasDTO = notas.stream()
                    .map(notaDTOConverter::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(notasDTO);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}