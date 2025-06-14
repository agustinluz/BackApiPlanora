package com.ejemplos.service;

import com.ejemplos.DTO.Gasto.ResumenDeudaDTO;
import com.ejemplos.modelo.DeudaGasto;
import com.ejemplos.modelo.DeudaGastoRepository;
import com.ejemplos.modelo.Gasto;
import com.ejemplos.modelo.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Servicio para la gestión de deudas de gastos
 * Maneja la creación, actualización y consulta de deudas entre usuarios
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeudaGastoService {
    
    private static final int PRECISION_DECIMAL = 2;
    private static final RoundingMode MODO_REDONDEO = RoundingMode.HALF_UP;
    
    private final DeudaGastoRepository deudaGastoRepository;
    
    /**
     * Crea automáticamente las deudas para un gasto dado
     * @param gasto El gasto para el cual crear las deudas
     * @throws IllegalArgumentException si el gasto es nulo o inválido
     */
    public void crearDeudasParaGasto(Gasto gasto) {
        validarGasto(gasto);
        
        log.info("Creando deudas para gasto ID: {} - Título: {}", 
                gasto.getId(), gasto.getTitulo());
        
        try {
            if (gasto.isPartesIguales()) {
                crearDeudasPartesIguales(gasto);
            } else {
                crearDeudasPersonalizadas(gasto);
            }
            
            log.info("Deudas creadas exitosamente para gasto ID: {}", gasto.getId());
            
        } catch (Exception e) {
            log.error("Error al crear deudas para gasto ID: {}", gasto.getId(), e);
            throw new RuntimeException("Error al crear deudas para el gasto", e);
        }
    }
    
    /**
     * Marca una deuda específica como saldada
     * @param gastoId ID del gasto
     * @param deudorId ID del deudor
     * @param metodoPago Método utilizado para el pago
     * @param notas Notas adicionales sobre el pago
     * @return true si se marcó como saldada, false si no se encontró la deuda
     */
    public boolean marcarComoSaldado(Long gastoId, Long deudorId, String metodoPago, String notas) {
        if (gastoId == null || deudorId == null) {
            log.warn("Parámetros inválidos para marcar deuda como saldada: gastoId={}, deudorId={}", 
                    gastoId, deudorId);
            return false;
        }
        
        log.info("Marcando deuda como saldada - Gasto ID: {}, Deudor ID: {}", gastoId, deudorId);
        
        Optional<DeudaGasto> deudaOpt = buscarDeudaPendiente(gastoId, deudorId);
        
        if (deudaOpt.isEmpty()) {
            log.warn("No se encontró deuda pendiente para gasto ID: {} y deudor ID: {}", 
                    gastoId, deudorId);
            return false;
        }
        
        DeudaGasto deuda = deudaOpt.get();
        saldarDeuda(deuda, metodoPago, notas);
        
        log.info("Deuda marcada como saldada exitosamente - ID: {}, Monto: {}", 
                deuda.getId(), deuda.getMonto());
        
        return true;
    }
    
    /**
     * Obtiene las deudas pendientes de un usuario específico
     * @param deudorId ID del usuario deudor
     * @return Lista de deudas pendientes
     */
    @Transactional(readOnly = true)
    public List<DeudaGasto> obtenerDeudasPendientes(Long deudorId) {
        if (deudorId == null) {
            log.warn("ID de deudor nulo al obtener deudas pendientes");
            return Collections.emptyList();
        }
        
        try {
            List<DeudaGasto> deudas = deudaGastoRepository.findByDeudorIdAndSaldadoFalse(deudorId);
            log.debug("Encontradas {} deudas pendientes para usuario ID: {}", deudas.size(), deudorId);
            return deudas;
            
        } catch (Exception e) {
            log.error("Error al obtener deudas pendientes para usuario ID: {}", deudorId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Obtiene los créditos pendientes de un usuario específico
     * @param acreedorId ID del usuario acreedor
     * @return Lista de créditos pendientes
     */
    @Transactional(readOnly = true)
    public List<DeudaGasto> obtenerCreditosPendientes(Long acreedorId) {
        if (acreedorId == null) {
            log.warn("ID de acreedor nulo al obtener créditos pendientes");
            return Collections.emptyList();
        }
        
        try {
            List<DeudaGasto> creditos = deudaGastoRepository.findByAcreedorIdAndSaldadoFalse(acreedorId);
            log.debug("Encontrados {} créditos pendientes para usuario ID: {}", creditos.size(), acreedorId);
            return creditos;
            
        } catch (Exception e) {
            log.error("Error al obtener créditos pendientes para usuario ID: {}", acreedorId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Obtiene todas las deudas asociadas a un gasto específico
     * @param gastoId ID del gasto
     * @return Lista de deudas del gasto
     */
    @Transactional(readOnly = true)
    public List<DeudaGasto> obtenerDeudasPorGasto(Long gastoId) {
        if (gastoId == null) {
            log.warn("ID de gasto nulo al obtener deudas por gasto");
            return Collections.emptyList();
        }
        
        try {
            List<DeudaGasto> deudas = deudaGastoRepository.findByGastoId(gastoId);
            log.debug("Encontradas {} deudas para gasto ID: {}", deudas.size(), gastoId);
            return deudas;
            
        } catch (Exception e) {
            log.error("Error al obtener deudas para gasto ID: {}", gastoId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Actualiza las deudas cuando se modifica un gasto
     * @param gasto Gasto modificado
     */
    public void actualizarDeudasParaGasto(Gasto gasto) {
        validarGasto(gasto);
        
        log.info("Actualizando deudas para gasto ID: {}", gasto.getId());
        
        try {
            eliminarDeudasExistentes(gasto.getId());
            crearDeudasParaGasto(gasto);
            
            log.info("Deudas actualizadas exitosamente para gasto ID: {}", gasto.getId());
            
        } catch (Exception e) {
            log.error("Error al actualizar deudas para gasto ID: {}", gasto.getId(), e);
            throw new RuntimeException("Error al actualizar deudas del gasto", e);
        }
    }
    
    /**
     * Elimina todas las deudas asociadas a un gasto
     * @param gastoId ID del gasto
     */
    public void eliminarDeudasPorGasto(Long gastoId) {
        if (gastoId == null) {
            log.warn("ID de gasto nulo al eliminar deudas");
            return;
        }
        
        log.info("Eliminando deudas para gasto ID: {}", gastoId);
        
        try {
            eliminarDeudasExistentes(gastoId);
            log.info("Deudas eliminadas exitosamente para gasto ID: {}", gastoId);
            
        } catch (Exception e) {
            log.error("Error al eliminar deudas para gasto ID: {}", gastoId, e);
            throw new RuntimeException("Error al eliminar deudas del gasto", e);
        }
    }
    
    // Métodos privados
    
    private void validarGasto(Gasto gasto) {
        if (gasto == null) {
            throw new IllegalArgumentException("El gasto no puede ser nulo");
        }
        
        if (gasto.getPagadoPor() == null) {
            throw new IllegalArgumentException("El gasto debe tener un pagador definido");
        }
        
        if (CollectionUtils.isEmpty(gasto.getUsuarios())) {
            throw new IllegalArgumentException("El gasto debe tener al menos un participante");
        }
        
        if (gasto.getMonto() == null || gasto.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del gasto debe ser mayor a cero");
        }
    }
    
    private void crearDeudasPartesIguales(Gasto gasto) {
    Usuario pagador = gasto.getPagadoPor();
    BigDecimal montoPorPersona = calcularMontoPorPersona(gasto.getMonto(), gasto.getUsuarios().size());

    for (Usuario participante : gasto.getUsuarios()) {
        if (!Objects.equals(participante.getId(), pagador.getId())) {

            // ✅ Verifica si ya existe deuda para este gasto y deudor
            boolean yaExiste = deudaGastoRepository.findByGastoId(gasto.getId()).stream()
                .anyMatch(d -> Objects.equals(d.getDeudor().getId(), participante.getId()));

            if (yaExiste) continue;

            DeudaGasto deuda = construirDeuda(gasto, participante, pagador, montoPorPersona);
            deudaGastoRepository.save(deuda);
        }
    }
}


    
    private void crearDeudasPersonalizadas(Gasto gasto) {
    Usuario pagador = gasto.getPagadoPor();
    Map<Long, BigDecimal> cantidades = gasto.getCantidadesPersonalizadas();

    if (CollectionUtils.isEmpty(cantidades)) return;

    for (Usuario participante : gasto.getUsuarios()) {
        Long participanteId = participante.getId();
        BigDecimal monto = cantidades.get(participanteId);

        if (esDeudaValida(participanteId, pagador.getId(), monto)) {

            // ✅ Verifica si ya existe deuda para este gasto y deudor
            boolean yaExiste = deudaGastoRepository.findByGastoId(gasto.getId()).stream()
                .anyMatch(d -> Objects.equals(d.getDeudor().getId(), participanteId));

            if (yaExiste) continue;

            DeudaGasto deuda = construirDeuda(gasto, participante, pagador, monto.setScale(PRECISION_DECIMAL, MODO_REDONDEO));
            deudaGastoRepository.save(deuda);
        }
    }
}

    
    private boolean esDeudaValida(Long participanteId, Long pagadorId, BigDecimal monto) {
        return !Objects.equals(participanteId, pagadorId) && 
               monto != null && 
               monto.compareTo(BigDecimal.ZERO) > 0;
    }
    
    private DeudaGasto construirDeuda(Gasto gasto, Usuario deudor, Usuario acreedor, BigDecimal monto) {
        DeudaGasto deuda = new DeudaGasto();
        deuda.setGasto(gasto);
        deuda.setDeudor(deudor);
        deuda.setAcreedor(acreedor);
        deuda.setMonto(monto);
        deuda.setSaldado(false);
        return deuda;
    }
    
    private BigDecimal calcularMontoPorPersona(BigDecimal montoTotal, int numeroParticipantes) {
        return montoTotal.divide(BigDecimal.valueOf(numeroParticipantes), PRECISION_DECIMAL, MODO_REDONDEO);
    }
    
    private Optional<DeudaGasto> buscarDeudaPendiente(Long gastoId, Long deudorId) {
        List<DeudaGasto> deudas = deudaGastoRepository.findByGastoId(gastoId);
        
        return deudas.stream()
                .filter(d -> Objects.equals(d.getDeudor().getId(), deudorId) && !d.isSaldado())
                .findFirst();
    }
    
    private void saldarDeuda(DeudaGasto deuda, String metodoPago, String notas) {
        deuda.setSaldado(true);
        deuda.setFechaSaldado(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        deuda.setMetodoPago(metodoPago);
        deuda.setNotas(notas);
        
        deudaGastoRepository.save(deuda);
    }
    
    private void eliminarDeudasExistentes(Long gastoId) {
    List<DeudaGasto> deudasExistentes = deudaGastoRepository.findByGastoId(gastoId);
    if (!deudasExistentes.isEmpty()) {
        deudaGastoRepository.deleteAll(deudasExistentes); // OK
    }
}

    
    public List<ResumenDeudaDTO> generarResumenPorGrupo(Long grupoId) {
        // Solo consideramos las deudas pendientes para evitar duplicados
        List<DeudaGasto> deudas = deudaGastoRepository.findDeudasPendientesByGrupoId(grupoId);
        Map<Long, ResumenDeudaDTO> resumenMap = new HashMap<>();

        for (DeudaGasto deuda : deudas) {
            Long deudorId   = deuda.getDeudor().getId();
            Long acreedorId = deuda.getAcreedor().getId();
            double monto    = deuda.getMonto().doubleValue();

            // Acumular en "debe" para el deudor
            ResumenDeudaDTO rDeudor = resumenMap.computeIfAbsent(deudorId, id ->
                    new ResumenDeudaDTO(id,
                                        deuda.getDeudor().getNombre(),
                                        deuda.getDeudor().getEmail(),
                                        0.0,
                                        0.0));
            rDeudor.setDebe(rDeudor.getDebe() + monto);

            // Acumular en "leDeben" para el acreedor
            ResumenDeudaDTO rAcreedor = resumenMap.computeIfAbsent(acreedorId, id ->
                    new ResumenDeudaDTO(id,
                                        deuda.getAcreedor().getNombre(),
                                        deuda.getAcreedor().getEmail(),
                                        0.0,
                                        0.0));
            rAcreedor.setLeDeben(rAcreedor.getLeDeben() + monto);
        }

        return new ArrayList<>(resumenMap.values());
    }



    
    
}