package com.ejemplos.service;

import com.ejemplos.modelo.Gasto;
import com.ejemplos.modelo.GastoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de gastos
 * Proporciona operaciones CRUD y lógica de negocio para gastos
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GastoService {
    
    private final GastoRepository gastoRepository;
    private final DeudaGastoService deudaGastoService;
    
    /**
     * Crea un nuevo gasto y genera automáticamente las deudas asociadas
     * @param gasto El gasto a crear
     * @return El gasto creado con su ID asignado
     * @throws IllegalArgumentException si el gasto es inválido
     */
    public Gasto crear(Gasto gasto) {
        validarGastoParaCreacion(gasto);
        
        log.info("Creando nuevo gasto - Título: {}, Monto: {}", 
                gasto.getTitulo(), gasto.getMonto());
        
        try {
            Gasto gastoGuardado = gastoRepository.save(gasto);
            
            // Generar deudas automáticamente después de crear el gasto
            deudaGastoService.crearDeudasParaGasto(gastoGuardado);
            
            log.info("Gasto creado exitosamente - ID: {}, Título: {}", 
                    gastoGuardado.getId(), gastoGuardado.getTitulo());
            
            return gastoGuardado;
            
        } catch (Exception e) {
            log.error("Error al crear gasto - Título: {}", gasto.getTitulo(), e);
            throw new RuntimeException("Error al crear el gasto", e);
        }
    }
    
    /**
     * Actualiza un gasto existente y recalcula las deudas
     * @param gasto El gasto a actualizar
     * @return El gasto actualizado
     * @throws IllegalArgumentException si el gasto es inválido
     */
    public Gasto actualizar(Gasto gasto) {
        validarGastoParaActualizacion(gasto);
        
        log.info("Actualizando gasto - ID: {}, Título: {}", gasto.getId(), gasto.getTitulo());
        
        try {
            Gasto gastoActualizado = gastoRepository.save(gasto);
            
            // Recalcular deudas después de la actualización
            deudaGastoService.actualizarDeudasParaGasto(gastoActualizado);
            
            log.info("Gasto actualizado exitosamente - ID: {}", gastoActualizado.getId());
            
            return gastoActualizado;
            
        } catch (Exception e) {
            log.error("Error al actualizar gasto - ID: {}", gasto.getId(), e);
            throw new RuntimeException("Error al actualizar el gasto", e);
        }
    }
    
    /**
     * Elimina un gasto y todas sus deudas asociadas
     * @param id ID del gasto a eliminar
     * @throws IllegalArgumentException si el ID es nulo
     */
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del gasto no puede ser nulo");
        }
        
        log.info("Eliminando gasto - ID: {}", id);
        
        try {
            // Verificar que el gasto existe antes de eliminar
            if (!gastoRepository.existsById(id)) {
                log.warn("Intento de eliminar gasto inexistente - ID: {}", id);
                throw new IllegalArgumentException("El gasto con ID " + id + " no existe");
            }
            
            // Eliminar deudas asociadas primero
            deudaGastoService.eliminarDeudasPorGasto(id);
            
            // Eliminar el gasto
            gastoRepository.deleteById(id);
            
            log.info("Gasto eliminado exitosamente - ID: {}", id);
            
        } catch (Exception e) {
            log.error("Error al eliminar gasto - ID: {}", id, e);
            throw new RuntimeException("Error al eliminar el gasto", e);
        }
    }
    
    /**
     * Obtiene un gasto por su ID
     * @param id ID del gasto a buscar
     * @return Optional con el gasto si existe
     */
    @Transactional(readOnly = true)
    public Optional<Gasto> obtenerPorId(Long id) {
        if (id == null) {
            log.warn("ID nulo al buscar gasto");
            return Optional.empty();
        }
        
        try {
            Optional<Gasto> gasto = gastoRepository.findById(id);
            
            if (gasto.isPresent()) {
                log.debug("Gasto encontrado - ID: {}, Título: {}", id, gasto.get().getTitulo());
            } else {
                log.debug("Gasto no encontrado - ID: {}", id);
            }
            
            return gasto;
            
        } catch (Exception e) {
            log.error("Error al obtener gasto por ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * Obtiene todos los gastos de un grupo específico
     * @param grupoId ID del grupo
     * @return Lista de gastos del grupo
     */
    @Transactional(readOnly = true)
    public List<Gasto> obtenerPorGrupo(Long grupoId) {
        if (grupoId == null) {
            log.warn("ID de grupo nulo al obtener gastos");
            return Collections.emptyList();
        }
        
        try {
            List<Gasto> gastos = gastoRepository.findByGrupoId(grupoId);
            
            log.debug("Encontrados {} gastos para grupo ID: {}", gastos.size(), grupoId);
            
            return gastos;
            
        } catch (Exception e) {
            log.error("Error al obtener gastos por grupo ID: {}", grupoId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Obtiene todos los gastos de un grupo y evento específicos
     * @param grupoId ID del grupo
     * @param eventoId ID del evento
     * @return Lista de gastos del grupo y evento
     */
    @Transactional(readOnly = true)
    public List<Gasto> obtenerPorGrupoYEvento(Long grupoId, Long eventoId) {
        if (grupoId == null || eventoId == null) {
            log.warn("Parámetros nulos al obtener gastos por grupo y evento - Grupo: {}, Evento: {}", 
                    grupoId, eventoId);
            return Collections.emptyList();
        }
        
        try {
            List<Gasto> gastos = gastoRepository.findByGrupoIdAndEventoId(grupoId, eventoId);
            
            log.debug("Encontrados {} gastos para grupo ID: {} y evento ID: {}", 
                    gastos.size(), grupoId, eventoId);
            
            return gastos;
            
        } catch (Exception e) {
            log.error("Error al obtener gastos por grupo ID: {} y evento ID: {}", 
                    grupoId, eventoId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Obtiene todos los gastos del sistema
     * @return Lista de todos los gastos
     */
    @Transactional(readOnly = true)
    public List<Gasto> obtenerTodos() {
        try {
            List<Gasto> gastos = gastoRepository.findAll();
            
            log.debug("Obtenidos {} gastos en total", gastos.size());
            
            return gastos;
            
        } catch (Exception e) {
            log.error("Error al obtener todos los gastos", e);
            return Collections.emptyList();
        }
    }
    
    public long contarPagadosPorUsuarioYGrupo(Long usuarioId, Long grupoId) {
        return gastoRepository.countByGrupoIdAndPagadoPorId(grupoId, usuarioId);
    }
    
    /**
     * Verifica si existe un gasto con el ID especificado
     * @param id ID del gasto a verificar
     * @return true si existe, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean existePorId(Long id) {
        if (id == null) {
            return false;
        }
        
        try {
            boolean existe = gastoRepository.existsById(id);
            
            log.debug("Gasto ID: {} {} existe", id, existe ? "sí" : "no");
            
            return existe;
            
        } catch (Exception e) {
            log.error("Error al verificar existencia de gasto ID: {}", id, e);
            return false;
        }
    }
    
    // Métodos privados de validación
    
    private void validarGastoParaCreacion(Gasto gasto) {
        if (gasto == null) {
            throw new IllegalArgumentException("El gasto no puede ser nulo");
        }
        
        validarDatosBasicos(gasto);
    }
    
    private void validarGastoParaActualizacion(Gasto gasto) {
        if (gasto == null) {
            throw new IllegalArgumentException("El gasto no puede ser nulo");
        }
        
        if (gasto.getId() == null) {
            throw new IllegalArgumentException("El ID del gasto es requerido para actualización");
        }
        
        validarDatosBasicos(gasto);
    }
    
    private void validarDatosBasicos(Gasto gasto) {
        if (gasto.getMonto() == null || gasto.getMonto().signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        
        if (gasto.getTitulo() == null || gasto.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El título del gasto es requerido");
        }
        
        if (gasto.getPagadoPor() == null) {
            throw new IllegalArgumentException("Debe especificarse quién pagó el gasto");
        }
        
        if (gasto.getGrupo() == null) {
            throw new IllegalArgumentException("El gasto debe estar asociado a un grupo");
        }
        
        if (gasto.getUsuarios() == null || gasto.getUsuarios().isEmpty()) {
            throw new IllegalArgumentException("El gasto debe tener al menos un participante");
        }
    }
}