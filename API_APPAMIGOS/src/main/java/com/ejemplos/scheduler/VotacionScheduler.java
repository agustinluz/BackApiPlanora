package com.ejemplos.scheduler;

import com.ejemplos.modelo.Votacion;
import com.ejemplos.service.VotacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class VotacionScheduler {

    @Autowired
    private VotacionService votacionService;

    // Ejecutar cada 5 minutos (300000 ms)
    @Scheduled(fixedRate = 300000)
    public void cerrarVotacionesExpiradas() {
        System.out.println("Verificando votaciones expiradas...");
        
        try {
            // Obtener todas las votaciones activas
            List<Votacion> votacionesActivas = votacionService.obtenerVotacionesActivas();
            Date ahora = new Date();
            
            for (Votacion votacion : votacionesActivas) {
                // Si tiene fecha de cierre y ya ha pasado
                if (votacion.getFechaCierre() != null && ahora.after(votacion.getFechaCierre())) {
                    votacion.setEstado(Votacion.EstadoVotacion.CERRADA);
                    votacionService.actualizar(votacion);
                    System.out.println("Votación cerrada automáticamente: " + votacion.getId());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error al cerrar votaciones expiradas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}