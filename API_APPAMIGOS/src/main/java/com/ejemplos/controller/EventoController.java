package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.Evento.EventoCreateDTO;
import com.ejemplos.DTO.Evento.EventoDTO;
import com.ejemplos.DTO.Evento.EventoDTOConverter;
import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.UsuarioGrupo;
import com.ejemplos.modelo.UsuarioGrupoRepository;
import com.ejemplos.security.JwtUtil;
import com.ejemplos.service.EventoAsistenteService;
import com.ejemplos.service.EventoService;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EventoDTOConverter eventoDTOConverter;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UsuarioGrupoRepository usuarioGrupoRepo;

    @Autowired
    private EventoAsistenteService eventoAsistenteService;

    
    @PostMapping("/{grupoId}/crear")
    public ResponseEntity<EventoDTO> crearEventoEnGrupo(
            @PathVariable Long grupoId,
            @RequestBody EventoCreateDTO dto,
            @RequestHeader("Authorization") String token) {
        try {
            // Validar que el grupo existe
            Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
            if (grupo == null) {
                return ResponseEntity.notFound().build();
            }

            // Validar token y usuario
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Crear evento usando el converter
            Evento evento = eventoDTOConverter.convertToEntity(dto);
            evento.setGrupo(grupo);
            
            evento.setCreador(usuario);
            
            Evento eventoGuardado = eventoService.crear(evento);
            EventoDTO response = eventoDTOConverter.convertToDTO(eventoGuardado);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{eventoId}/asistencia")
    public ResponseEntity<?> marcarAsistencia(
            @PathVariable Long eventoId,
            @RequestParam boolean asistio,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Evento evento = eventoService.obtenerPorId(eventoId).orElse(null);
            if (evento == null) {
                return ResponseEntity.notFound().build();
            }

            eventoAsistenteService.marcarAsistencia(evento, usuario, asistio);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/{eventoId}/asistentes")
    public ResponseEntity<?> obtenerAsistentes(@PathVariable Long eventoId) {
        try {
            var lista = eventoAsistenteService.obtenerPorEvento(eventoId);
            java.util.List<com.ejemplos.DTO.Evento.EventoAsistenteDTO> dtos = lista.stream().map(ea -> {
                var dto = new com.ejemplos.DTO.Evento.EventoAsistenteDTO();
                dto.setUsuarioId(ea.getUsuario().getId());
                dto.setNombre(ea.getUsuario().getNombre());
                dto.setFotoPerfil(ea.getUsuario().getFotoPerfil());
                dto.setAsistio(ea.isAsistio());
                return dto;
            }).toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{eventoId}/asistencia/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas(@PathVariable Long eventoId) {
        try {
            long total = eventoAsistenteService.contarInvitados(eventoId);
            long asistieron = eventoAsistenteService.contarAsistentes(eventoId);
            return ResponseEntity.ok(
                    java.util.Map.of(
                            "eventoId", eventoId,
                            "totalInvitados", total,
                            "asistieron", asistieron));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
    
    @GetMapping("/{grupoId}/eventos")
    public ResponseEntity<List<EventoDTO>> listarEventosGrupo(@PathVariable Long grupoId) {
        List<Evento> eventos = eventoService.obtenerPorGrupo(grupoId);
        List<EventoDTO> dtos = eventos.stream()
            .map(eventoDTOConverter::convertToDTO)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{eventoId}")
    public ResponseEntity<EventoDTO> obtenerEvento(@PathVariable Long eventoId) {
        return eventoService.obtenerPorId(eventoId)
                .map(eventoDTOConverter::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{eventoId}")
    public ResponseEntity<EventoDTO> actualizarEvento(
        @PathVariable Long eventoId,
        @RequestBody EventoCreateDTO dto,
        @RequestHeader("Authorization") String token) {
      // 1. Valida token → usuario
      String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
      Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
      if (usuario == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      // 2. Busca el evento
      Evento eventoExistente = eventoService.obtenerPorId(eventoId).orElse(null);
      if (eventoExistente == null) {
        return ResponseEntity.notFound().build();
      }

      // 3. Consulta el rol del usuario en el grupo del evento
      Long grupoId = eventoExistente.getGrupo().getId();
      UsuarioGrupo ug = usuarioGrupoRepo
          .findByUsuarioIdAndGrupoId(usuario.getId(), grupoId)
          .orElse(null);
      if (ug == null) {
        // no pertenece al grupo → forbidden
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }

      // 4. Comprueba que sea cre­ador o admin del grupo
      boolean esCreador = usuario.getId().equals(eventoExistente.getCreador().getId());
      boolean esAdmin   = "admin".equalsIgnoreCase(ug.getRol());
      if (!esCreador && !esAdmin) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }

      // 5. Actualiza campos permitidos
      eventoExistente.setTitulo(dto.getTitulo());
      eventoExistente.setDescripcion(dto.getDescripcion());
      eventoExistente.setFecha(dto.getFecha());
      eventoExistente.setUbicacion(dto.getUbicacion());

      // 6. Guarda y devuelve DTO
      Evento actualizado = eventoService.actualizar(eventoExistente);
      EventoDTO response = eventoDTOConverter.convertToDTO(actualizado);
      return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventoId}")
    public ResponseEntity<Void> eliminarEvento(
            @PathVariable Long eventoId,
            @RequestHeader("Authorization") String token) {
        try {
            // Validar token y usuario
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (!eventoService.existePorId(eventoId)) {
                return ResponseEntity.notFound().build();
            }

            eventoService.eliminar(eventoId);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}