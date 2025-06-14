package com.ejemplos.modelo;

import com.ejemplos.modelo.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, Long> {
    
    // Métodos para Grupo
    List<Imagen> findByGrupoIdOrderByFechaCreacionDesc(Long grupoId);
    List<Imagen> findByGrupoId(Long grupoId);
    long countByGrupoId(Long grupoId);
    List<Imagen> findTop10ByGrupoIdOrderByFechaCreacionDesc(Long grupoId);
    
    // Métodos para Usuario
    List<Imagen> findByUsuarioId(Long usuarioId);
    List<Imagen> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    List<Imagen> findTop10ByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    long countByUsuarioId(Long usuarioId);
    
    // Métodos para Evento
    List<Imagen> findByEventoId(Long eventoId);
    List<Imagen> findByEventoIdOrderByFechaCreacionDesc(Long eventoId);
    List<Imagen> findTop10ByEventoIdOrderByFechaCreacionDesc(Long eventoId);
    long countByEventoId(Long eventoId);
    
    // Métodos combinados
    List<Imagen> findByGrupoIdAndEventoId(Long grupoId, Long eventoId);
    List<Imagen> findByGrupoIdAndUsuarioId(Long grupoId, Long usuarioId);
    List<Imagen> findByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);
    long countByGrupoIdAndUsuarioId(Long grupoId, Long usuarioId);
    
    // Consultas personalizadas con @Query si necesitas algo más específico
    @Query("SELECT i FROM Imagen i WHERE i.grupo.id = :grupoId AND i.tamaño < :maxSize ORDER BY i.fechaCreacion DESC")
    List<Imagen> findByGrupoIdAndSizeLessThanOrderByFechaCreacionDesc(@Param("grupoId") Long grupoId, @Param("maxSize") Long maxSize);
    
    @Query("SELECT i FROM Imagen i WHERE i.tipoContenido LIKE :contentType%")
    List<Imagen> findByTipoContenidoStartingWith(@Param("contentType") String contentType);
    
    // Para obtener solo los metadatos sin los datos Base64 (optimización)
    @Query("SELECT new com.ejemplos.modelo.Imagen(i.id, i.nombre, i.tipoContenido, i.tamaño, i.fechaCreacion) FROM Imagen i WHERE i.grupo.id = :grupoId ORDER BY i.fechaCreacion DESC")
    List<Imagen> findImagenMetadataByGrupoId(@Param("grupoId") Long grupoId);
}