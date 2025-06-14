// DeudaGastoRepository - Métodos sugeridos para optimizar las consultas

package com.ejemplos.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DeudaGastoRepository extends JpaRepository<DeudaGasto, Long> {
    
    // Métodos optimizados para el servicio mejorado
    
    /**
     * Encuentra deudas pendientes por deudor (más eficiente)
     */
    List<DeudaGasto> findByDeudorIdAndSaldadoFalse(Long deudorId);
    
    /**
     * Encuentra créditos pendientes por acreedor (más eficiente)
     */
    List<DeudaGasto> findByAcreedorIdAndSaldadoFalse(Long acreedorId);
    
    /**
     * Encuentra todas las deudas de un gasto específico
     */
    List<DeudaGasto> findByGastoId(Long gastoId);
    
    /**
     * Todas las deudas (saldadas o no) de los gastos cuyo grupo.id = grupoId
     */
    List<DeudaGasto> findByGasto_Grupo_Id(Long grupoId);
    
    /**
     * Encuentra deudas pendientes entre dos usuarios específicos
     */
    @Query("SELECT d FROM DeudaGasto d WHERE d.deudor.id = :deudorId AND d.acreedor.id = :acreedorId AND d.saldado = false")
    List<DeudaGasto> findDeudasPendientesEntreUsuarios(@Param("deudorId") Long deudorId, @Param("acreedorId") Long acreedorId);
    
    /**
     * Calcula el total de deudas pendientes de un usuario
     */
    @Query("SELECT COALESCE(SUM(d.monto), 0) FROM DeudaGasto d WHERE d.deudor.id = :deudorId AND d.saldado = false")
    BigDecimal calcularTotalDeudasPendientes(@Param("deudorId") Long deudorId);
    
    /**
     * Calcula el total de créditos pendientes de un usuario
     */
    @Query("SELECT COALESCE(SUM(d.monto), 0) FROM DeudaGasto d WHERE d.acreedor.id = :acreedorId AND d.saldado = false")
    BigDecimal calcularTotalCreditosPendientes(@Param("acreedorId") Long acreedorId);
    
    /**
     * Encuentra deudas pendientes por grupo
     */
    @Query("SELECT d FROM DeudaGasto d WHERE d.gasto.grupo.id = :grupoId AND d.saldado = false")
    List<DeudaGasto> findDeudasPendientesByGrupoId(@Param("grupoId") Long grupoId);
    
    /**
     * Cuenta el número de deudas pendientes de un usuario
     */
    @Query("SELECT COUNT(d) FROM DeudaGasto d WHERE d.deudor.id = :deudorId AND d.saldado = false")
    Long contarDeudasPendientes(@Param("deudorId") Long deudorId);
    
    /**
     * Verifica si existe alguna deuda pendiente para un gasto específico
     */
    @Query("SELECT COUNT(d) > 0 FROM DeudaGasto d WHERE d.gasto.id = :gastoId AND d.saldado = false")
    boolean existenDeudasPendientesParaGasto(@Param("gastoId") Long gastoId);
    

}