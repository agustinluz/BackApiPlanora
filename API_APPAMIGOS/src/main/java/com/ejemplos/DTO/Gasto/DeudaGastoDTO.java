package com.ejemplos.DTO.Gasto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class DeudaGastoDTO {
    private Long id;
    private Long deudorId;
    private String deudorNombre;
    private Long acreedorId;
    private String acreedorNombre;
    private Long gastoId;
    private String titulo;
    private BigDecimal monto;
    private boolean saldado;
    private String metodoPago;
}