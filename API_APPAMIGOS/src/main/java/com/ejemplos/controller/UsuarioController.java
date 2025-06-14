package com.ejemplos.controller;

import com.ejemplos.DTO.Usuario.UsuarioDTO;
import com.ejemplos.DTO.Usuario.UsuarioUpdateDTO;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.service.UsuarioService;
import com.ejemplos.DTO.Usuario.UsuarioDTOConverter;
import com.ejemplos.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioDTOConverter usuarioDTOConverter;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerUsuario(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            // Extraer el token sin el prefijo "Bearer "
            String jwtToken = token.replace("Bearer ", "");
            String emailFromToken = jwtUtil.extractEmail(jwtToken);
            
            Optional<Usuario> usuario = usuarioService.obtenerPorId(id);
            if (usuario.isPresent()) {
                // Verificar que el usuario autenticado puede acceder a estos datos
                if (!usuario.get().getEmail().equals(emailFromToken)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                
                UsuarioDTO dto = usuarioDTOConverter.convertToDTO(usuario.get());
                // Obtener grupos del usuario desde la entidad UsuarioGrupo
                List<Long> gruposIds = usuario.get().getUsuarioGrupos().stream()
                    .map(ug -> ug.getGrupo().getId())
                    .toList();
                dto.setGrupoIds(gruposIds);
                
                return ResponseEntity.ok(dto);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioUpdateDTO updateDTO, @RequestHeader("Authorization") String token) {
        try {
            // Extraer el token sin el prefijo "Bearer "
            String jwtToken = token.replace("Bearer ", "");
            String emailFromToken = jwtUtil.extractEmail(jwtToken);
            
            Optional<Usuario> usuarioExistente = usuarioService.obtenerPorId(id);
            
            if (!usuarioExistente.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioExistente.get();
            
            // Verificar que el usuario autenticado puede modificar estos datos
            if (!usuario.getEmail().equals(emailFromToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("No tienes permisos para modificar este usuario"));
            }

            // Verificar si el email ya existe en otro usuario
            if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(usuario.getEmail())) {
                Optional<Usuario> usuarioConEmail = usuarioService.obtenerPorEmail(updateDTO.getEmail());
                if (usuarioConEmail.isPresent() && !usuarioConEmail.get().getId().equals(id)) {
                    return ResponseEntity.badRequest()
                        .body(new ErrorResponse("El email ya está en uso por otro usuario"));
                }
                usuario.setEmail(updateDTO.getEmail());
            }

         // Actualizar contraseña si se proporciona
            if (updateDTO.getPassword() != null && !updateDTO.getPassword().trim().isEmpty()) {
                // Verificar contraseña actual antes de cambiarla
                if (updateDTO.getCurrentPassword() == null ||
                    !passwordEncoder.matches(updateDTO.getCurrentPassword(), usuario.getPassword())) {
                    return ResponseEntity.badRequest()
                        .body(new ErrorResponse("La contraseña actual no es correcta"));
                }

                // Validar que la nueva contraseña tenga complejidad adecuada
                if (!updateDTO.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
                    return ResponseEntity.badRequest()
                        .body(new ErrorResponse("La nueva contraseña debe tener al menos 8 caracteres, una mayúscula y un número"));
                }

                if (passwordEncoder.matches(updateDTO.getPassword(), usuario.getPassword())) {
                    return ResponseEntity.badRequest()
                        .body(new ErrorResponse("La nueva contraseña no puede ser igual a la anterior"));
                }


                // Guardar la nueva contraseña cifrada
                usuario.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
            }

            // Usar el método actualizar() en lugar de crear()
            Usuario usuarioActualizado = usuarioService.actualizar(usuario);
            UsuarioDTO dto = usuarioDTOConverter.convertToDTO(usuarioActualizado);
            
            // Obtener grupos del usuario desde la entidad UsuarioGrupo
            List<Long> gruposIds = usuarioActualizado.getUsuarioGrupos().stream()
                .map(ug -> ug.getGrupo().getId())
                .toList();
            dto.setGrupoIds(gruposIds);
            
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error al actualizar el usuario: " + e.getMessage()));
        }
    }
    // Clase interna para respuestas de error
    public static class ErrorResponse {
        private String mensaje;

        public ErrorResponse(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }
    }
}