package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.Gasto.DeudaGastoDTO;
import com.ejemplos.DTO.Gasto.DeudaGastoDTOConverter;
import com.ejemplos.DTO.Gasto.GastoCreateDTO;
import com.ejemplos.DTO.Gasto.GastoDTO;
import com.ejemplos.DTO.Gasto.GastoDTOConverter;
import com.ejemplos.DTO.Gasto.ResumenDeudaDTO;
import com.ejemplos.DTO.Usuario.UsuarioDTO;
import com.ejemplos.DTO.Usuario.UsuarioDTOConverter;
import com.ejemplos.modelo.DeudaGasto;
import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.Gasto;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.service.DeudaGastoService;
import com.ejemplos.service.EventoService;
import com.ejemplos.service.GastoService;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.UsuarioService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gasto")
public class GastoController {

    @Autowired
    private GastoService gastoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private DeudaGastoService deudaGastoService;
    
    @Autowired
    private GastoDTOConverter gastoDTOConverter;
    
    @Autowired
    private UsuarioDTOConverter usuarioDTOConverter;
    
    @Autowired
    private DeudaGastoDTOConverter deudaGastoDTOConverter;

    @PostMapping("/{grupoId}/crear")
    public ResponseEntity<GastoDTO> crearGasto(
            @PathVariable Long grupoId,
            @RequestBody GastoCreateDTO gastoDTO) {

        Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
        if (grupo == null) return ResponseEntity.notFound().build();

        Usuario pagadoPor = usuarioService.obtenerPorId(gastoDTO.getPagadoPorId()).orElse(null);
        if (pagadoPor == null) return ResponseEntity.badRequest().build();

        // Obtener participantes
        List<Usuario> participantes = usuarioService.obtenerPorIds(gastoDTO.getParticipantesIds());
        if (participantes == null || participantes.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Validar cantidades personalizadas si no es partes iguales
        if (!gastoDTO.isPartesIguales()) {
            Map<Long, BigDecimal> cantidades = gastoDTO.getCantidadesPersonalizadas();
            if (cantidades == null || cantidades.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            BigDecimal suma = cantidades.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (suma.compareTo(gastoDTO.getMonto()) != 0) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        Gasto gasto = new Gasto();
        gasto.setTitulo(gastoDTO.getTitulo());
        gasto.setMonto(gastoDTO.getMonto());
        gasto.setGrupo(grupo);
        gasto.setPagadoPor(pagadoPor);
        gasto.setUsuarios(participantes);
        gasto.setPartesIguales(gastoDTO.isPartesIguales());

        if (!gastoDTO.isPartesIguales()) {
            gasto.setCantidadesPersonalizadas(gastoDTO.getCantidadesPersonalizadas());
        }

        // Asociar evento si corresponde
        if (gastoDTO.getEventoId() != null) {
            Evento evento = eventoService.obtenerPorId(gastoDTO.getEventoId()).orElse(null);
            if (evento != null) {
                gasto.setEvento(evento);
            }
        }

        Gasto guardado = gastoService.crear(gasto);
        deudaGastoService.crearDeudasParaGasto(guardado);

        return ResponseEntity.ok(gastoDTOConverter.convertToDTO(guardado));
    }

    @GetMapping("/{grupoId}/gastos")
    public ResponseEntity<List<GastoDTO>> listarGastosGrupo(@PathVariable Long grupoId) {
        Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
        if (grupo == null) return ResponseEntity.notFound().build();
        
        List<Gasto> gastos = gastoService.obtenerPorGrupo(grupoId);
        List<GastoDTO> gastosDTO = gastos.stream()
                .map(gastoDTOConverter::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(gastosDTO);
    } 	

    @GetMapping("/{gastoId}")
    public ResponseEntity<GastoDTO> obtenerGasto(@PathVariable Long gastoId) {
        return gastoService.obtenerPorId(gastoId)
                .map(gasto -> ResponseEntity.ok(gastoDTOConverter.convertToDTO(gasto)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{gastoId}")
    public ResponseEntity<GastoDTO> actualizarGasto(
            @PathVariable Long gastoId,
            @RequestBody GastoCreateDTO gastoDTO) {
        
        Optional<Gasto> gastoExistente = gastoService.obtenerPorId(gastoId);
        if (gastoExistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Gasto gasto = gastoExistente.get();
        gasto.setTitulo(gastoDTO.getTitulo());
        gasto.setMonto(gastoDTO.getMonto());

        // Actualizar pagador si cambió
        Usuario pagadoPor = usuarioService.obtenerPorId(gastoDTO.getPagadoPorId()).orElse(null);
        if (pagadoPor != null) {
            gasto.setPagadoPor(pagadoPor);
        }

        // Actualizar participantes
        List<Usuario> participantes = usuarioService.obtenerPorIds(gastoDTO.getParticipantesIds());
        gasto.setUsuarios(participantes);

        // Actualizar evento si cambió
        if (gastoDTO.getEventoId() != null) {
            Evento evento = eventoService.obtenerPorId(gastoDTO.getEventoId()).orElse(null);
            gasto.setEvento(evento);
        } else {
            gasto.setEvento(null);
        }

        Gasto actualizado = gastoService.actualizar(gasto);
        return ResponseEntity.ok(gastoDTOConverter.convertToDTO(actualizado));
    }

    @DeleteMapping("/{gastoId}")
    public ResponseEntity<Void> eliminarGasto(@PathVariable Long gastoId) {
        if (!gastoService.existePorId(gastoId)) {
            return ResponseEntity.notFound().build();
        }
        
        gastoService.eliminar(gastoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grupos/{gastoId}/participantes")
    public ResponseEntity<List<UsuarioDTO>> obtenerParticipantesGasto(@PathVariable Long gastoId) {
        Optional<Gasto> gastoOpt = gastoService.obtenerPorId(gastoId);
        if (gastoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<UsuarioDTO> participantesDTO = usuarioDTOConverter.convertToDTOList(gastoOpt.get().getUsuarios());
        return ResponseEntity.ok(participantesDTO);
    }

    @PostMapping("/grupos/{gastoId}/participantes/{participanteId}/saldado")
    public ResponseEntity<Map<String, Object>> marcarComoSaldado(
            @PathVariable Long gastoId,
            @PathVariable Long participanteId,
            @RequestBody(required = false) Map<String, String> body) {
        
        // Verificar que el gasto existe
        Optional<Gasto> gastoOpt = gastoService.obtenerPorId(gastoId);
        if (gastoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String metodoPago = body != null ? body.get("metodoPago") : "no especificado";
        String notas = body != null ? body.get("notas") : "";
        
        boolean saldado = deudaGastoService.marcarComoSaldado(gastoId, participanteId, metodoPago, notas);
        
        if (saldado) {
            // Devolver JSON en lugar de texto plano
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Marcado como saldado correctamente");
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "No se encontró la deuda o ya estaba saldada");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/grupos/{grupoId}/eventos/{eventoId}")
    public ResponseEntity<List<GastoDTO>> obtenerGastosPorEvento(
            @PathVariable Long grupoId, 
            @PathVariable Long eventoId) {
        
        List<Gasto> gastos = gastoService.obtenerPorGrupoYEvento(grupoId, eventoId);
        List<GastoDTO> gastosDTO = gastos.stream()
                .map(gastoDTOConverter::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(gastosDTO);
    }

    @GetMapping("/grupos/{grupoId}/deudas")
    public ResponseEntity<Map<String, Object>> obtenerResumenDeudas(@PathVariable Long grupoId) {
        List<Gasto> gastos = gastoService.obtenerPorGrupo(grupoId);
        
        Map<String, BigDecimal> balances = new HashMap<>();
        
        for (Gasto gasto : gastos) {
            String pagador = gasto.getPagadoPor().getNombre();
            BigDecimal montoPorPersona = gasto.getMonto().divide(
                BigDecimal.valueOf(gasto.getUsuarios().size()), 
                2, 
                BigDecimal.ROUND_HALF_UP
            );
            
            // El pagador tiene saldo positivo
            balances.put(pagador, balances.getOrDefault(pagador, BigDecimal.ZERO).add(
                gasto.getMonto().subtract(montoPorPersona)
            ));
            
            // Los participantes tienen saldo negativo
            for (Usuario participante : gasto.getUsuarios()) {
                if (!participante.getId().equals(gasto.getPagadoPor().getId())) {
                    balances.put(participante.getNombre(), 
                        balances.getOrDefault(participante.getNombre(), BigDecimal.ZERO)
                            .subtract(montoPorPersona)
                    );
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("balances", balances);
        response.put("totalGastos", gastos.stream()
            .map(Gasto::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gastoId}/deudas")
public ResponseEntity<List<DeudaGastoDTO>> obtenerDeudasGasto(@PathVariable Long gastoId) {
    List<DeudaGasto> deudas = deudaGastoService.obtenerDeudasPorGasto(gastoId)
                             .stream()
                             .distinct() // elimina duplicados si hubiera
                             .collect(Collectors.toList());
    List<DeudaGastoDTO> deudasDTO = deudaGastoDTOConverter.toDTOList(deudas);
    return ResponseEntity.ok(deudasDTO);
}

    
    @GetMapping("/grupos/{grupoId}/resumen")
    public ResponseEntity<List<ResumenDeudaDTO>> obtenerResumenDeGrupo(@PathVariable Long grupoId) {
        List<ResumenDeudaDTO> resumen = deudaGastoService.generarResumenPorGrupo(grupoId);
        return ResponseEntity.ok(resumen);
    }
    
    
}