package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.Votacion.VotacionCreateDTO;
import com.ejemplos.DTO.Votacion.VotacionDTO;
import com.ejemplos.DTO.Votacion.VotacionDTOConverter;
import com.ejemplos.DTO.Votacion.VotacionUpdateDTO;
import com.ejemplos.DTO.Votacion.VotoCreateDTO;
import com.ejemplos.DTO.Votacion.VotoDTO;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.Votacion;
import com.ejemplos.modelo.Votacion.EstadoVotacion;
import com.ejemplos.modelo.Voto;
import com.ejemplos.modelo.VotacionRepository;
import com.ejemplos.modelo.VotoRepository;
import com.ejemplos.security.JwtUtil;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.UsuarioService;
import com.ejemplos.service.VotacionService;
import com.ejemplos.service.VotoService;
import com.ejemplos.service.UsuarioGrupoService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")

public class VotacionController {

    @Autowired
    private VotacionService votacionService;

    @Autowired
    private VotoService votoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private UsuarioGrupoService usuarioGrupoService;

    @Autowired
    private VotacionDTOConverter votacionDTOConverter;

    @Autowired
    private VotacionRepository votacionRepository;

    @Autowired
    private VotoRepository votoRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Obtener votación por ID
    @GetMapping("/votaciones/{id}")
    public ResponseEntity<VotacionDTO> obtenerVotacion(@PathVariable Long id) {
        try {
            Optional<Votacion> votacion = votacionService.obtenerPorId(id);
            if (votacion.isPresent()) {
                VotacionDTO dto = votacionDTOConverter.convertToDTO(votacion.get());
                return ResponseEntity.ok(dto);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/grupos/{grupoId}/votaciones")
    public ResponseEntity<?> listarVotacionesGrupo(
            @PathVariable Long grupoId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            System.out.println("=== LISTAR VOTACIONES ===");
            System.out.println("Buscando votaciones para grupo ID: " + grupoId);
            System.out.println("Token: " + token);

            // Opcional: Validar autenticación para listar
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    String jwt = token.replace("Bearer ", "");
                    String email = jwtUtil.extractEmail(jwt);
                    Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
                    
                    if (usuario != null) {
                        boolean perteneceAlGrupo = usuarioGrupoService.usuarioPerteneceAlGrupo(usuario.getId(), grupoId);
                        if (!perteneceAlGrupo) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "No tienes acceso a las votaciones de este grupo"));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error al validar token para listar: " + e.getMessage());
                    
                }
            }

            List<Votacion> votaciones = votacionRepository.findByGrupoId(grupoId);
            System.out.println("Votaciones encontradas: " + votaciones.size());

            List<VotacionDTO> votacionesDTO = votaciones.stream()
                .map(votacionDTOConverter::convertToDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(votacionesDTO);
            
        } catch (Exception e) {
            System.err.println("Error al obtener votaciones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @PostMapping("/grupos/{grupoId}/votaciones")
    public ResponseEntity<?> crearVotacion(
            @PathVariable Long grupoId,
            @RequestBody VotacionCreateDTO votacionDTO,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            
            // Validar token
            if (token == null || !token.startsWith("Bearer ")) {
                System.out.println("Token inválido o faltante");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token de autorización requerido"));
            }

            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            System.out.println("JWT extraído: " + jwt);
            
            String email;
            try {
                email = jwtUtil.extractEmail(jwt);
                System.out.println("Email extraído del token: " + email);
            } catch (Exception e) {
                System.out.println("Error al extraer email del token: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token inválido"));
            }
            
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                System.out.println("Usuario no encontrado con email: " + email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no encontrado"));
            }
            
            System.out.println("Usuario encontrado: " + usuario.getNombre());

            // Verificar que el grupo existe
            Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
            if (grupo == null) {
                System.out.println("Grupo no encontrado con ID: " + grupoId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Grupo no encontrado"));
            }
            
            System.out.println("Grupo encontrado: " + grupo.getNombre());

            // Verificar que el usuario pertenece al grupo
            boolean perteneceAlGrupo = usuarioGrupoService.usuarioPerteneceAlGrupo(usuario.getId(), grupoId);
            if (!perteneceAlGrupo) {
                System.out.println("Usuario no pertenece al grupo");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permisos para crear votaciones en este grupo"));
            }
            
            System.out.println("Usuario pertenece al grupo");

            // Validar datos de la votación
            if (votacionDTO.getPregunta() == null || votacionDTO.getPregunta().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "La pregunta es obligatoria"));
            }
            
            if (votacionDTO.getOpciones() == null || votacionDTO.getOpciones().size() < 2) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Se requieren al menos 2 opciones"));
            }
            
            // Validar que las opciones no estén vacías
            List<String> opcionesValidas = votacionDTO.getOpciones().stream()
                .filter(opcion -> opcion != null && !opcion.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
                
            if (opcionesValidas.size() < 2) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Se requieren al menos 2 opciones válidas"));
            }

            // Crear votación
            Votacion votacion = new Votacion();
            votacion.setTitulo(votacionDTO.getPregunta().trim());
            votacion.setDescripcion(votacionDTO.getDescripcion()); // Si existe en el DTO
            votacion.setOpciones(opcionesValidas);
            votacion.setGrupo(grupo);
            votacion.setCreador(usuario);
            votacion.setFechaCreacion(new Date());
            votacion.setEstado(Votacion.EstadoVotacion.ACTIVA);
            
            // Establecer fecha límite si se proporcionó
            if (votacionDTO.getFechaLimite() != null) {
                votacion.setFechaCierre(votacionDTO.getFechaLimite());
            }

            System.out.println("Guardando votación...");
            Votacion guardada = votacionService.crear(votacion);
            System.out.println("Votación guardada con ID: " + guardada.getId());
            
            VotacionDTO response = votacionDTOConverter.convertToDTO(guardada);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error al crear votación: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    // Actualizar votación
    @PutMapping("/votaciones/{id}")
    public ResponseEntity<VotacionDTO> actualizarVotacion(
            @PathVariable Long id,
            @RequestBody VotacionUpdateDTO votacionDTO,
            @RequestHeader("Authorization") String token) {
        try {
            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Obtener votación
            Votacion votacion = votacionService.obtenerPorId(id).orElse(null);
            if (votacion == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar que es el creador
            if (!votacion.getCreador().getId().equals(usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verificar que la votación está activa
            if (!"ACTIVA".equals(votacion.getEstado())) {
                return ResponseEntity.badRequest().build();
            }

            // Validar opciones (mínimo 2)
            if (votacionDTO.getOpciones() == null || votacionDTO.getOpciones().size() < 2) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar campos
            if (votacionDTO.getTitulo() != null) {
                votacion.setTitulo(votacionDTO.getTitulo());
            }
            if (votacionDTO.getDescripcion() != null) {
                votacion.setDescripcion(votacionDTO.getDescripcion());
            }
            if (votacionDTO.getOpciones() != null) {
                votacion.setOpciones(votacionDTO.getOpciones());
            }

            Votacion actualizada = votacionService.actualizar(votacion);
            VotacionDTO response = votacionDTOConverter.convertToDTO(actualizada);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Cerrar votación
 // Cerrar votación
    @PutMapping("/votaciones/{id}/cerrar")
    public ResponseEntity<?> cerrarVotacion(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autorizado"));
            }

            Votacion votacion = votacionService.obtenerPorId(id).orElse(null);
            if (votacion == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Votación no encontrada"));
            }

            // Verificar que el usuario es el creador
            if (!votacion.getCreador().equals(usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo el creador puede cerrar la votación"));
            }

            votacion.setEstado(EstadoVotacion.CERRADA);
            votacion.setFechaCierre(new Date());
            votacionService.actualizar(votacion);

            return ResponseEntity.ok(Map.of("message", "Votación cerrada correctamente"));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al cerrar votación: " + e.getMessage()));
        }
    }

    // Eliminar votación
    @DeleteMapping("/votaciones/{id}")
    public ResponseEntity<Void> eliminarVotacion(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Obtener votación
            Votacion votacion = votacionService.obtenerPorId(id).orElse(null);
            if (votacion == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar que es el creador
            if (!votacion.getCreador().getId().equals(usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Eliminar votos asociados primero
            votoRepository.deleteByVotacionId(id);
            
            // Eliminar votación
            votacionService.eliminar(id);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============ ENDPOINTS DE VOTOS ============

 // Votar en una votación
    @PostMapping("/votaciones/{id}/votar")
    public ResponseEntity<?> votar(
            @PathVariable Long id,
            @RequestBody VotoCreateDTO votoDTO,
            @RequestHeader("Authorization") String token) {
        try {
            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autorizado"));
            }

            // Obtener votación
            Votacion votacion = votacionService.obtenerPorId(id).orElse(null);
            if (votacion == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Votación no encontrada"));
            }

            // CORRECCIÓN: Verificar que la votación está activa comparando con el enum
            if (!Votacion.EstadoVotacion.ACTIVA.equals(votacion.getEstado())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "La votación no está activa. Estado actual: " + votacion.getEstado()));
            }

            // MEJORA: Verificar si la votación ha expirado por fecha
            if (votacion.getFechaCierre() != null && new Date().after(votacion.getFechaCierre())) {
                // Actualizar estado a CERRADA si ha expirado
                votacion.setEstado(Votacion.EstadoVotacion.CERRADA);
                votacionService.actualizar(votacion);
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "La votación ha expirado"));
            }

            // Verificar que el usuario pertenece al grupo
            boolean perteneceAlGrupo = usuarioGrupoService.usuarioPerteneceAlGrupo(
                usuario.getId(), votacion.getGrupo().getId());
            if (!perteneceAlGrupo) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permiso para votar en esta votación"));
            }

            // Verificar que no ha votado ya
            Optional<Voto> votoExistente = votoService.obtenerVotoUsuario(id, usuario.getId());
            if (votoExistente.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Ya has votado en esta votación"));
            }

            // Validar que la opción existe
            if (votacion.getOpciones() == null || !votacion.getOpciones().contains(votoDTO.getOpcion())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Opción no válida"));
            }

            // Crear voto
            Voto voto = new Voto(votacion, usuario, votoDTO.getOpcion());
            Voto guardado = votoService.crear(voto);

            // Crear respuesta
            VotoDTO response = new VotoDTO();
            response.setId(guardado.getId());
            response.setVotacionId(guardado.getVotacion().getId());
            response.setUsuarioId(guardado.getUsuario().getId());
            response.setOpcion(guardado.getOpcion());
            response.setFechaVoto(guardado.getFechaVoto());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    // Obtener resultados de una votación
    @GetMapping("/votaciones/{id}/resultados")
    public ResponseEntity<?> obtenerResultados(@PathVariable Long id) {
        try {
            Votacion votacion = votacionService.obtenerPorId(id).orElse(null);
            if (votacion == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Votación no encontrada"));
            }

            List<Voto> votos = votoService.obtenerPorVotacionId(id);
            
            // Contar votos por opción
            Map<String, Long> conteoVotos = votos.stream()
                .collect(Collectors.groupingBy(Voto::getOpcion, Collectors.counting()));

            // Agregar opciones sin votos con valor 0
            if (votacion.getOpciones() != null) {
                for (String opcion : votacion.getOpciones()) {
                    conteoVotos.putIfAbsent(opcion, 0L);
                }
            }

            Map<String, Object> resultados = new HashMap<>();
            resultados.put("votacionId", id);
            resultados.put("titulo", votacion.getTitulo());
            resultados.put("pregunta", votacion.getTitulo());
            resultados.put("estado", votacion.getEstado());
            resultados.put("totalVotos", votos.size());
            resultados.put("resultados", conteoVotos);
            resultados.put("fechaCreacion", votacion.getFechaCreacion());
            resultados.put("fechaCierre", votacion.getFechaCierre());

            return ResponseEntity.ok(resultados);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener resultados: " + e.getMessage()));
        }
    }

    // Obtener mi voto en una votación
    @GetMapping("/votaciones/{votacionId}/mi-voto")
    public ResponseEntity<?> obtenerMiVoto(
            @PathVariable Long votacionId, // Cambié 'id' por 'votacionId'
            @RequestHeader("Authorization") String token) {
        try {
            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no autorizado"));
            }

            // Verificar que la votación existe
            Votacion votacion = votacionService.obtenerPorId(votacionId).orElse(null);
            if (votacion == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Votación no encontrada"));
            }

            Optional<Voto> voto = votoService.obtenerVotoUsuario(votacionId, usuario.getId());

            if (voto.isPresent()) {
                VotoDTO response = new VotoDTO();
                response.setId(voto.get().getId());
                response.setVotacionId(voto.get().getVotacion().getId());
                response.setUsuarioId(voto.get().getUsuario().getId());
                response.setOpcion(voto.get().getOpcion());
                response.setFechaVoto(voto.get().getFechaVoto());

                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No has votado en esta votación"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener voto: " + e.getMessage()));
        }
    }
}