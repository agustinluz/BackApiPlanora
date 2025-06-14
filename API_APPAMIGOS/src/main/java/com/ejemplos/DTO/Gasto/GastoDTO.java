package com.ejemplos.DTO.Gasto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ejemplos.DTO.Evento.EventoDTO;
import com.ejemplos.DTO.Grupo.GrupoDTO;
import com.ejemplos.DTO.Usuario.UsuarioDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GastoDTO {
    private Long id;
    private String titulo;
    private BigDecimal monto;
    private UsuarioDTO pagadoPor;
    private GrupoDTO grupo;
    private EventoDTO evento;
    private List<UsuarioDTO> usuarios;
    private List<DeudaGastoDTO> deudas;
    private boolean partesIguales;
    private Map<Long, BigDecimal> cantidadesPersonalizadas;
    private Date fechaCreacion;

}