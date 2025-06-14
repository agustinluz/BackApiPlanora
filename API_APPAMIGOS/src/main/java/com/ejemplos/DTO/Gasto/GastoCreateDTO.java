package com.ejemplos.DTO.Gasto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GastoCreateDTO {
    private String titulo;
    private BigDecimal monto;
    private Long pagadoPorId;
    private Long grupoId;
    private Long eventoId; // puede ser null
    private boolean partesIguales;
    private List<Long> participantesIds;
    private Map<Long, BigDecimal> cantidadesPersonalizadas; // solo si partesIguales es false

}
