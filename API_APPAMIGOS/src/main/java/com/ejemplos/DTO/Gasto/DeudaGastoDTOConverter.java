package com.ejemplos.DTO.Gasto;

import com.ejemplos.modelo.DeudaGasto;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeudaGastoDTOConverter {
    
    public DeudaGastoDTO convertToDTO(DeudaGasto deudaGasto) {
        if (deudaGasto == null) {
            return null;
        }
        
        DeudaGastoDTO dto = new DeudaGastoDTO();
        dto.setId(deudaGasto.getId());
        dto.setDeudorId(deudaGasto.getDeudor().getId());
        dto.setDeudorNombre(deudaGasto.getDeudor().getNombre());
        dto.setAcreedorId(deudaGasto.getAcreedor().getId());
        dto.setAcreedorNombre(deudaGasto.getAcreedor().getNombre());
        dto.setGastoId(deudaGasto.getGasto().getId());
        dto.setTitulo(deudaGasto.getGasto().getTitulo());
        dto.setMonto(deudaGasto.getMonto());
        dto.setSaldado(deudaGasto.isSaldado());
        dto.setMetodoPago(deudaGasto.getMetodoPago());
        
        return dto;
    }
    
    public List<DeudaGastoDTO> toDTOList(List<DeudaGasto> deudaGastos) {
        return deudaGastos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}