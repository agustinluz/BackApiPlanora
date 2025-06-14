package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.Grupo.GrupoDTO;
import com.ejemplos.DTO.Grupo.GrupoDTOConverter;
import com.ejemplos.DTO.Usuario.UsuarioCreateDTO;
import com.ejemplos.DTO.Usuario.UsuarioDTO;
import com.ejemplos.DTO.Usuario.UsuarioLoginDTO;
import com.ejemplos.DTO.Usuario.UsuarioResetPasswordDTO;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.security.JwtUtil;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.UsuarioService;

import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private GrupoService grupoService;
    
    @Autowired
    private GrupoDTOConverter grupoDTOConverter;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioLoginDTO loginDTO) {
        Optional<Usuario> usuarioOpt = usuarioService.login(loginDTO.getEmail(), loginDTO.getPassword());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }

        Usuario usuario = usuarioOpt.get();
        String token = jwtUtil.generateToken(usuario.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);

        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(usuario.getId());
        usuarioDTO.setNombre(usuario.getNombre());
        usuarioDTO.setEmail(usuario.getEmail());

        // Obtener grupos del usuario desde la entidad UsuarioGrupo
        List<Long> gruposIds = usuario.getUsuarioGrupos().stream()
            .map(ug -> ug.getGrupo().getId())
            .toList();

        usuarioDTO.setGrupoIds(gruposIds);
        response.put("usuario", usuarioDTO);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody UsuarioCreateDTO dto) {
    	if (!isValidPassword(dto.getPassword())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número"));
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());

        try {
            Usuario creado = usuarioService.crear(usuario);

            UsuarioDTO response = new UsuarioDTO();
            response.setId(creado.getId());
            response.setNombre(creado.getNombre());
            response.setEmail(creado.getEmail());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody UsuarioResetPasswordDTO dto) {
        Optional<Usuario> usuarioOpt = usuarioService.findByEmail(dto.getEmail());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        if (!isValidPassword(dto.getPassword())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número"));
        }

        if (passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "La nueva contraseña no puede ser igual a la anterior"));
        }

        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuarioService.actualizar(usuario);

        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada"));
    }
    
    @GetMapping("/invitacion/{codigo}")
    public ResponseEntity<GrupoDTO> obtenerPorCodigoInvitacion(@PathVariable String codigo) {
        return grupoService.obtenerPorCodigoInvitacion(codigo)
                .map(grupoDTOConverter::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
}