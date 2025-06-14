package com.ejemplos.modelo;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
@Table(name = "votos")
public class Voto implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "votacion_id", nullable = false)
    private Votacion votacion;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "opcion", nullable = false)
    private String opcion;
    
    @Column(name = "fecha_voto", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVoto;
    
    // Constructor por defecto
    public Voto() {
        this.fechaVoto = new Date();
    }
    
    // Constructor con parámetros
    public Voto(Votacion votacion, Usuario usuario, String opcion) {
        this.votacion = votacion;
        this.usuario = usuario;
        this.opcion = opcion;
        this.fechaVoto = new Date();
    }
}