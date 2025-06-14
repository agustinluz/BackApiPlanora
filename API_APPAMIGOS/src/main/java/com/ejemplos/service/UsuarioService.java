package com.ejemplos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.UsuarioGrupo;
import com.ejemplos.modelo.UsuarioGrupoRepository;
import com.ejemplos.modelo.UsuarioRepository;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private UsuarioGrupoRepository usuarioGrupoRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }
    
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    /**
     * Obtiene los usuarios pertenecientes a un grupo.
     *
     * @param grupoId identificador del grupo
     * @return lista de usuarios que pertenecen al grupo
     */
    public List<Usuario> obtenerPorGrupoId(Long grupoId) {
        return usuarioGrupoRepository.findByGrupoId(grupoId)
                .stream()
                .map(UsuarioGrupo::getUsuario)
                .toList();
    }
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    // Método para crear un nuevo usuario (cifra la contraseña)
    public Usuario crear(Usuario usuario) {
        // Solo cifra la contraseña si es un usuario nuevo o si la contraseña ha cambiado
        if (usuario.getId() == null) {
            // Usuario nuevo - cifrar contraseña
            String encodedPassword = passwordEncoder.encode(usuario.getPassword());
            usuario.setPassword(encodedPassword);
        }
        return usuarioRepository.save(usuario);
    }
    
    // Método específico para actualizar usuario (no cifra automáticamente)
    public Usuario actualizar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    
    // Método para crear usuario con contraseña ya cifrada
    public Usuario crearConPasswordCifrada(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }
    
    public Optional<Usuario> login(String email, String password) {
        Optional<Usuario> user = usuarioRepository.findByEmail(email);
        if (user.isPresent()) {
            Usuario usuario = user.get();
            // Verifica si la contraseña proporcionada coincide con la cifrada
            if (passwordEncoder.matches(password, usuario.getPassword())) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty(); // Si no coincide o no existe, devuelve vacío
    }
    
    public List<Usuario> obtenerPorIds(List<Long> ids) {
        return usuarioRepository.findAllById(ids);
    }
    
    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    public List<Usuario> obtenerUsuariosPorGrupo(Long grupoId) {
        return usuarioGrupoRepository.findByGrupoId(grupoId)
                .stream()
                .map(UsuarioGrupo::getUsuario)
                .toList();
    }
}