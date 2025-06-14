package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.Grupo.GrupoCreateDTO;
import com.ejemplos.DTO.Grupo.GrupoDTO;
import com.ejemplos.DTO.Grupo.GrupoDTOConverter;
import com.ejemplos.DTO.Usuario.UsuarioCreateDTO;
import com.ejemplos.DTO.Usuario.UsuarioDTO;
import com.ejemplos.DTO.Usuario.UsuarioDTOConverter;
import com.ejemplos.DTO.Usuario.UsuarioGrupoDTO;
import com.ejemplos.DTO.Usuario.UsuarioGrupoDTOConverter;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Invitacion;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.UsuarioGrupo;
import com.ejemplos.service.AsistenciaEventoService;
import com.ejemplos.service.GastoService;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.ImagenService;
import com.ejemplos.service.InvitacionService;
import com.ejemplos.service.NotaService;
import com.ejemplos.service.UsuarioGrupoService;
import com.ejemplos.service.UsuarioService;
import com.ejemplos.service.VotacionService;
import com.ejemplos.service.VotoService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupos")
public class GrupoController {

	@Autowired
	private GrupoService grupoService;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private UsuarioGrupoService usuarioGrupoService;

	@Autowired
	private GrupoDTOConverter grupoDTOConverter;

	@Autowired
	private UsuarioDTOConverter usuarioDTOConverter;

	@Autowired
	private UsuarioGrupoDTOConverter usuarioGrupoDTOConverter;

	@Autowired
    private AsistenciaEventoService asistenciaEventoService;
	
	@Autowired
    private InvitacionService invitacionService;
	
	
	@Autowired
    private GastoService gastoService;

    @Autowired
    private NotaService notaService;

    @Autowired
    private VotacionService votacionService;

    @Autowired
    private VotoService votoService;

    @Autowired
    private ImagenService imagenService;
    
    
	@GetMapping("/buscar-usuario")
    public ResponseEntity<UsuarioDTO> buscarUsuarioPorEmail(@RequestParam String email) {
            return usuarioService.obtenerPorEmail(email)
                            .map(usuarioDTOConverter::convertToDTO)
                            .map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
    }
	// Obtener grupo por ID
	@GetMapping("/{id}")
	public ResponseEntity<GrupoDTO> obtenerGrupo(@PathVariable Long id) {
		return grupoService.obtenerPorId(id).map(grupoDTOConverter::convertToDTO).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	// Obtener grupos de un usuario
	@GetMapping("/usuario/{id}")
	public ResponseEntity<List<GrupoDTO>> obtenerGruposDeUsuario(@PathVariable Long id) {
		List<UsuarioGrupo> asociaciones = usuarioGrupoService.obtenerPorUsuarioId(id);
		List<GrupoDTO> gruposDTO = asociaciones.stream().map(UsuarioGrupo::getGrupo)
				.map(grupoDTOConverter::convertToDTO).collect(Collectors.toList());
		return ResponseEntity.ok(gruposDTO);
	}

	// Crear nuevo grupo con usuario como admin
	@PostMapping
	public ResponseEntity<GrupoDTO> crearGrupo(@RequestBody GrupoCreateDTO dto,
			@RequestHeader("usuarioId") Long usuarioId) {

		if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
			return ResponseEntity.badRequest().build();
		}

		Usuario admin = usuarioService.obtenerPorId(usuarioId).orElse(null);
		if (admin == null) {
			return ResponseEntity.badRequest().build();
		}

		Grupo grupo = grupoDTOConverter.convertToEntity(dto);
		Grupo guardado = grupoService.crear(grupo);

		// Asociamos al creador como admin en la tabla intermedia
		UsuarioGrupo ug = new UsuarioGrupo();
		ug.setGrupo(guardado);
		ug.setUsuario(admin);
		ug.setRol("admin");
		usuarioGrupoService.guardar(ug);

		return ResponseEntity.ok(grupoDTOConverter.convertToDTO(guardado));
	}

	// Actualizar grupo (solo admin)
	@PutMapping("/{id}")
	public ResponseEntity<GrupoDTO> actualizarGrupo(@PathVariable Long id, @RequestBody GrupoCreateDTO dto,
			@RequestHeader("usuarioId") Long usuarioId) {

		Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
		if (grupo == null) {
			return ResponseEntity.notFound().build();
		}

		// Verificar que el usuario es admin
		Optional<UsuarioGrupo> ugOpt = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, id)
				.filter(ug -> "admin".equalsIgnoreCase(ug.getRol()));
		if (ugOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
			grupo.setNombre(dto.getNombre());
		}
		if (dto.getImagenPerfil() != null) {
			grupo.setImagenPerfil(dto.getImagenPerfil());
		}

		Grupo actualizado = grupoService.crear(grupo);
		return ResponseEntity.ok(grupoDTOConverter.convertToDTO(actualizado));
	}

	// Eliminar participante del grupo (solo admin)
	@DeleteMapping("/{grupoId}/usuarios/{usuarioId}")
	public ResponseEntity<Void> eliminarParticipante(@PathVariable Long grupoId, @PathVariable Long usuarioId,
			@RequestHeader("adminId") Long adminId) {

		Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
		if (grupo == null) {
			return ResponseEntity.notFound().build();
		}

		// Verificar rol admin
		Optional<UsuarioGrupo> adminUG = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(adminId, grupoId)
				.filter(ug -> "admin".equalsIgnoreCase(ug.getRol()));
		if (adminUG.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		if (adminId.equals(usuarioId)) {
			return ResponseEntity.badRequest().build();
		}

		usuarioGrupoService.eliminarUsuarioDeGrupo(usuarioId, grupoId);
		return ResponseEntity.ok().build();
	}

	// Listar usuarios del grupo (información básica)
	@GetMapping("/{id}/usuarios")
	public ResponseEntity<List<UsuarioDTO>> listarUsuariosGrupo(@PathVariable Long id) {
		List<Usuario> usuarios = usuarioGrupoService.obtenerUsuariosPorGrupoId(id);
		return ResponseEntity.ok(usuarioDTOConverter.convertToDTOList(usuarios));
	}

	// Obtener participantes con roles (acceso restringido a miembros)
	@GetMapping("/{grupoId}/participantes-con-roles")
	public ResponseEntity<List<UsuarioGrupoDTO>> obtenerParticipantesConRoles(@PathVariable Long grupoId,
			@RequestHeader("usuarioId") Long usuarioId) {

		// Sólo miembros pueden ver roles
		boolean esMiembro = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, grupoId).isPresent();
		if (!esMiembro) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		List<UsuarioGrupo> asociaciones = usuarioGrupoService.obtenerPorGrupoId(grupoId);
		return ResponseEntity.ok(usuarioGrupoDTOConverter.convertToDTOList(asociaciones));
	}

	// Registrar usuario en grupo (crear usuario si no existe)
	@PostMapping("/{id}/usuarios")
    public ResponseEntity<?> registrarUsuarioEnGrupo(@PathVariable Long id,
                    @RequestBody UsuarioCreateDTO usuarioDTO) {

	Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
	if (grupo == null) {
		return ResponseEntity.notFound().build();
	}

            Usuario usuario = usuarioService.obtenerPorEmail(usuarioDTO.getEmail()).orElse(null);
            if (usuario == null) {
                    Usuario u = new Usuario();
                    u.setNombre(usuarioDTO.getNombre());
                    u.setEmail(usuarioDTO.getEmail());
                    u.setPassword(usuarioDTO.getPassword());
                    try {
                            usuario = usuarioService.crear(u);
                    } catch (IllegalArgumentException e) {
                            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
                    }
            }

	UsuarioGrupo ug = new UsuarioGrupo();
	ug.setGrupo(grupo);
	ug.setUsuario(usuario);
	ug.setRol("miembro");
	usuarioGrupoService.guardar(ug);

	return ResponseEntity.ok(usuarioDTOConverter.convertToDTO(usuario));
}

	// Invitar usuario existente al grupo
	@PostMapping("/{grupoId}/invitar")
	public ResponseEntity<UsuarioGrupoDTO> invitarUsuarioExistente(@PathVariable Long grupoId,
			@RequestParam String emailUsuario, @RequestHeader("adminId") Long adminId) {

		Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
		if (grupo == null) {
			return ResponseEntity.notFound().build();
		}

		// Verificar admin
		boolean esAdmin = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(adminId, grupoId)
				.filter(ug -> "admin".equalsIgnoreCase(ug.getRol())).isPresent();
		if (!esAdmin) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		Usuario usuario = usuarioService.obtenerPorEmail(emailUsuario).orElse(null);
		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}

		boolean yaMiembro = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuario.getId(), grupoId).isPresent();
		if (yaMiembro) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}

		Invitacion invitacion = invitacionService.crearInvitacion(grupo, usuario);
        UsuarioGrupoDTO dto = new UsuarioGrupoDTO();
        dto.setGrupoId(grupo.getId());
        dto.setUsuarioId(usuario.getId());
        dto.setNombreUsuario(usuario.getNombre());
        dto.setEmailUsuario(usuario.getEmail());
        dto.setRol("pendiente");
        return ResponseEntity.ok(dto);
		
	}

	// Cambiar rol de usuario
	@PutMapping("/{grupoId}/usuarios/{usuarioId}/rol")
	public ResponseEntity<UsuarioGrupoDTO> cambiarRolUsuario(@PathVariable Long grupoId, @PathVariable Long usuarioId,
			@RequestParam String nuevoRol, @RequestHeader("adminId") Long adminId) {

		if (!List.of("admin", "miembro").contains(nuevoRol.toLowerCase())) {
			return ResponseEntity.badRequest().build();
		}

		// Verificar que solicitante es admin
		boolean esAdmin = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(adminId, grupoId)
				.filter(ug -> "admin".equalsIgnoreCase(ug.getRol())).isPresent();
		if (!esAdmin) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		if (adminId.equals(usuarioId) && nuevoRol.equalsIgnoreCase("miembro")) {
			return ResponseEntity.badRequest().build();
		}

		UsuarioGrupo ug = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, grupoId).orElse(null);
		if (ug == null) {
			return ResponseEntity.notFound().build();
		}

		ug.setRol(nuevoRol);
		usuarioGrupoService.guardar(ug);
		return ResponseEntity.ok(usuarioGrupoDTOConverter.convertToDTO(ug));
	}

	// Transferir administración
	@PutMapping("/{grupoId}/transferir-admin")
	public ResponseEntity<Map<String, String>> transferirAdministracion(@PathVariable Long grupoId,
			@RequestParam Long nuevoAdminId, @RequestHeader("adminId") Long adminActualId) {

		// Verificar admin actual
		boolean esAdmin = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(adminActualId, grupoId)
				.filter(ug -> "admin".equalsIgnoreCase(ug.getRol())).isPresent();
		if (!esAdmin) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		// Verificar que el nuevo admin es miembro
		UsuarioGrupo nuevoUG = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(nuevoAdminId, grupoId).orElse(null);
		if (nuevoUG == null) {
			return ResponseEntity.badRequest().build();
		}

		// Actualizar roles
		UsuarioGrupo actualUG = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(adminActualId, grupoId).get();

		actualUG.setRol("miembro");
		usuarioGrupoService.guardar(actualUG);

		nuevoUG.setRol("admin");
		usuarioGrupoService.guardar(nuevoUG);

		Map<String, String> resp = new HashMap<>();
		resp.put("mensaje", "Administración transferida exitosamente");
		resp.put("nuevoAdmin", usuarioService.obtenerPorId(nuevoAdminId).get().getNombre());
		return ResponseEntity.ok(resp);
	}

	// Salir del grupo
	@DeleteMapping("/{grupoId}/salir")
	public ResponseEntity<Void> salirDelGrupo(
	        @PathVariable Long grupoId,
	        @RequestHeader("usuarioId") Long usuarioId) {

	    // Buscamos la relación usuario–grupo
	    UsuarioGrupo ug = usuarioGrupoService
	        .obtenerPorUsuarioIdYGrupoId(usuarioId, grupoId)
	        .orElse(null);
	    if (ug == null) {
	        return ResponseEntity.notFound().build();
	    }

	    // Solo si eres admin Y hay más de un participante Y solo hay 1 admin, bloqueamos
	    if ("admin".equalsIgnoreCase(ug.getRol())) {
	        int totalParticipantes = usuarioGrupoService.contarParticipantesPorGrupo(grupoId);
	        int totalAdmins       = usuarioGrupoService.contarAdminsPorGrupo(grupoId);

	        if (totalParticipantes > 1 && totalAdmins == 1) {
	            // Eres el único admin pero hay otros miembros: no puedes irte sin ceder rol
	            return ResponseEntity
	                    .badRequest()
	                    .build();
	        }
	    }

	    // Procedemos a eliminar al usuario del grupo
	    usuarioGrupoService.eliminarUsuarioDeGrupo(usuarioId, grupoId);

	    // Si ya no queda nadie, borramos también el grupo
	    if (usuarioGrupoService.contarParticipantesPorGrupo(grupoId) == 0) {
	        grupoService.eliminar(grupoId);
	    }

	    return ResponseEntity.ok().build();
	}

	// Obtener información de un participante específico
	@GetMapping("/{grupoId}/usuarios/{usuarioId}")
	public ResponseEntity<UsuarioGrupoDTO> obtenerParticipante(@PathVariable Long grupoId, @PathVariable Long usuarioId,
			@RequestHeader("solicitanteId") Long solicitanteId) {

		boolean esMiembro = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(solicitanteId, grupoId).isPresent();
		if (!esMiembro) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		UsuarioGrupo ug = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, grupoId).orElse(null);
		if (ug == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(usuarioGrupoDTOConverter.convertToDTO(ug));
	}

	// Generar nuevo código de invitación
	@PostMapping("/{id}/generar-codigo")
	public ResponseEntity<Map<String, String>> generarNuevoCodigo(@PathVariable Long id,
			@RequestHeader("usuarioId") Long usuarioId) {

		// Verificar admin
		boolean esAdmin = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, id)
				.filter(ug -> "admin".equalsIgnoreCase(ug.getRol())).isPresent();
		if (!esAdmin) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
		if (grupo == null) {
			return ResponseEntity.notFound().build();
		}

		String nuevoCodigo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		grupo.setCodigoInvitacion(nuevoCodigo);
		grupoService.crear(grupo);

		return ResponseEntity.ok(Map.of("codigoInvitacion", nuevoCodigo));
	}

	// Obtener estadísticas del grupo
	@GetMapping("/{id}/estadisticas")
	public ResponseEntity<Map<String, Object>> obtenerEstadisticasGrupo(@PathVariable Long id,
			@RequestHeader("usuarioId") Long usuarioId) {

		boolean esMiembro = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, id).isPresent();
		if (!esMiembro) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
		if (grupo == null) {
			return ResponseEntity.notFound().build();
		}

		Map<String, Object> stats = new HashMap<>();
		stats.put("grupoId", grupo.getId());
		stats.put("nombreGrupo", grupo.getNombre());

		int total = usuarioGrupoService.contarParticipantesPorGrupo(id);
		int admins = usuarioGrupoService.contarAdminsPorGrupo(id);
		stats.put("totalParticipantes", total);
		stats.put("totalAdmins", admins);
		stats.put("totalMiembros", total - admins);

		stats.put("totalEventos", Optional.ofNullable(grupo.getEventos()).map(List::size).orElse(0));
		stats.put("totalGastos", Optional.ofNullable(grupo.getGastos()).map(List::size).orElse(0));
		stats.put("totalNotas", Optional.ofNullable(grupo.getNotas()).map(List::size).orElse(0));
		stats.put("totalVotaciones", Optional.ofNullable(grupo.getVotaciones()).map(List::size).orElse(0));

		return ResponseEntity.ok(stats);
	}
	
	// Obtener estadísticas por usuario en el grupo
    @GetMapping("/{id}/estadisticas/usuarios")
    public ResponseEntity<List<Map<String, Object>>> obtenerEstadisticasUsuarios(@PathVariable Long id,
                    @RequestHeader("usuarioId") Long usuarioId) {

            boolean esMiembro = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, id).isPresent();
            if (!esMiembro) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<UsuarioGrupo> asociaciones = usuarioGrupoService.obtenerPorGrupoId(id);
            List<Map<String, Object>> resultado = new ArrayList<>();

            for (UsuarioGrupo ug : asociaciones) {
                    Usuario u = ug.getUsuario();
                    Map<String, Object> userStats = new HashMap<>();
                    userStats.put("usuarioId", u.getId());
                    userStats.put("nombreUsuario", u.getNombre());
                    userStats.put("rol", ug.getRol());

                    long creados = Optional.ofNullable(u.getEventosCreados())
                                    .orElse(List.of())
                                    .stream()
                                    .filter(e -> e.getGrupo().getId().equals(id))
                                    .count();
                    userStats.put("eventosCreados", creados);

                    long asistencias = asistenciaEventoService.contarAsistenciasPorUsuarioYGrupo(u.getId(), id);
                    userStats.put("eventosAsistidos", asistencias);
                    
                    long notas = notaService.contarPorGrupoYUsuario(id, u.getId());
                    userStats.put("notasCreadas", notas);

                    long gastos = gastoService.contarPagadosPorUsuarioYGrupo(u.getId(), id);
                    userStats.put("gastosPagados", gastos);

                    long votaciones = votacionService.contarPorGrupoYUsuario(id, u.getId());
                    userStats.put("votacionesCreadas", votaciones);

                    long votos = votoService.contarVotosPorUsuarioYGrupo(u.getId(), id);
                    userStats.put("votosEmitidos", votos);

                    long imagenes = imagenService.contarImagenesPorGrupoYUsuario(id, u.getId());
                    userStats.put("imagenesSubidas", imagenes);

                    resultado.add(userStats);
            }

            return ResponseEntity.ok(resultado);
    }

	// Eliminar grupo (solo admin)
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminarGrupo(@PathVariable Long id, @RequestHeader("usuarioId") Long usuarioId) {

		boolean esAdmin = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, id)
				.filter(ug -> "admin".equalsIgnoreCase(ug.getRol())).isPresent();
		if (!esAdmin) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		grupoService.eliminar(id);
		return ResponseEntity.ok().build();
	}
}
