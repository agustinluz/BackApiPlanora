package com.ejemplos.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ejemplos.modelo.NotaRepository;
import com.ejemplos.modelo.Nota;

@Service
public class NotaService {
    
    @Autowired
    private NotaRepository notaRepository;
    
    // Obtener todas las notas
    public List<Nota> obtenerTodas() {
        return notaRepository.findAll();
    }
    
    // Obtener nota por ID
    public Optional<Nota> obtenerPorId(Long id) {
        return notaRepository.findById(id);
    }
    
    // Obtener notas por grupo
    public List<Nota> obtenerPorGrupo(Long grupoId) {
        return notaRepository.findByGrupoId(grupoId);
    }
    
    // Obtener notas por grupo ordenadas por ID (más recientes primero)
    public List<Nota> obtenerPorGrupoOrdenadas(Long grupoId) {
        return notaRepository.findByGrupoIdOrderByIdDesc(grupoId);
    }
    
    // Obtener notas por usuario
    public List<Nota> obtenerPorUsuario(Long usuarioId) {
        return notaRepository.findByUsuarioId(usuarioId);
    }
    
    // Obtener notas por usuario ordenadas por ID (más recientes primero)
    public List<Nota> obtenerPorUsuarioOrdenadas(Long usuarioId) {
        return notaRepository.findByUsuarioIdOrderByIdDesc(usuarioId);
    }
    
    // Obtener notas por grupo y usuario
    public List<Nota> obtenerPorGrupoYUsuario(Long grupoId, Long usuarioId) {
        return notaRepository.findByGrupoIdAndUsuarioId(grupoId, usuarioId);
    }
    
 // Obtener notas por grupo y evento
    public List<Nota> obtenerPorGrupoYEvento(Long grupoId, Long eventoId) {
        return notaRepository.findByGrupoIdAndEventoId(grupoId, eventoId);
    }

    public List<Nota> obtenerPorGrupoYEventoOrdenadas(Long grupoId, Long eventoId) {
        return notaRepository.findByGrupoIdAndEventoIdOrderByIdDesc(grupoId, eventoId);
    }
    
    // Crear nueva nota
    public Nota crear(Nota nota) {
    	nota.setFechaCreacion(new Date());
        return notaRepository.save(nota);
        
    }
    
    // Actualizar nota existente
    public Nota actualizar(Nota nota) {
        return notaRepository.save(nota);
    }
    
    // Eliminar nota por ID
    public void eliminar(Long id) {
        notaRepository.deleteById(id);
    }
    
    // Eliminar nota por objeto
    public void eliminar(Nota nota) {
        notaRepository.delete(nota);
    }
    
    // Verificar si existe una nota por ID
    public boolean existe(Long id) {
        return notaRepository.existsById(id);
    }
    
    // Buscar notas por título
    public List<Nota> buscarPorTitulo(String titulo) {
        return notaRepository.findByTituloContainingIgnoreCase(titulo);
    }
    
    // Buscar notas por contenido
    public List<Nota> buscarPorContenido(String contenido) {
        return notaRepository.findByContenidoContainingIgnoreCase(contenido);
    }
    
    // Contar notas por grupo
    public long contarPorGrupo(Long grupoId) {
        return obtenerPorGrupo(grupoId).size();
    }
    
    // Contar notas por usuario
    public long contarPorUsuario(Long usuarioId) {
        return obtenerPorUsuario(usuarioId).size();
    }
    public long contarPorGrupoYUsuario(Long grupoId, Long usuarioId) {
        return notaRepository.countByGrupoIdAndUsuarioId(grupoId, usuarioId);
    }
}