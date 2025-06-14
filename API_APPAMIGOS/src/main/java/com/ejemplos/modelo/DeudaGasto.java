package com.ejemplos.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Data
@Entity
@Table(name = "deudas_gastos")
public class DeudaGasto implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "gasto_id", nullable = false)
    @JsonIgnore // Para evitar referencia circular en JSON
    private Gasto gasto;
    
    @ManyToOne
    @JoinColumn(name = "deudor_id", nullable = false)
    private Usuario deudor;
    
    @ManyToOne
    @JoinColumn(name = "acreedor_id", nullable = false)
    private Usuario acreedor; // Quien pagó el gasto
    
    private BigDecimal monto; // Monto que debe
    
    private boolean saldado = false;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaSaldado;
    
    private String metodoPago; // "efectivo", "transferencia", etc.
    
    private String notas; // Notas adicionales sobre el pago
}