package com.ejemplos.modelo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {
    
    // Buscar notas por ID del grupo
    List<Nota> findByGrupoId(Long grupoId);

    // Buscar notas por ID del usuario
    List<Nota> findByUsuarioId(Long usuarioId);

    // Buscar notas por grupo y usuario
    List<Nota> findByGrupoIdAndUsuarioId(Long grupoId, Long usuarioId);

    // Buscar notas por título (búsqueda parcial, case-insensitive)
    List<Nota> findByTituloContainingIgnoreCase(String titulo);

    // Buscar notas por contenido (búsqueda parcial, case-insensitive) - CORREGIDO
    @Query("SELECT n FROM Nota n WHERE LOWER(CAST(n.contenido AS string)) LIKE LOWER(CONCAT('%', :contenido, '%'))")
    List<Nota> findByContenidoContainingIgnoreCase(@Param("contenido") String contenido);

    // Buscar notas por grupo ordenadas por ID descendente (más recientes primero)
    List<Nota> findByGrupoIdOrderByIdDesc(Long grupoId);

    // Buscar notas por usuario ordenadas por ID descendente (más recientes primero)
    List<Nota> findByUsuarioIdOrderByIdDesc(Long usuarioId);
    

    // Buscar notas por grupo y evento
    List<Nota> findByGrupoIdAndEventoId(Long grupoId, Long eventoId);
    
    long countByGrupoIdAndUsuarioId(Long grupoId, Long usuarioId);

    // Buscar notas por grupo y evento ordenadas
    List<Nota> findByGrupoIdAndEventoIdOrderByIdDesc(Long grupoId, Long eventoId);
    
    // Método adicional para búsqueda en título y contenido combinada
    @Query("SELECT n FROM Nota n WHERE " +
           "LOWER(n.titulo) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(CAST(n.contenido AS string)) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Nota> findByTituloOrContenidoContainingIgnoreCase(@Param("texto") String texto);
}