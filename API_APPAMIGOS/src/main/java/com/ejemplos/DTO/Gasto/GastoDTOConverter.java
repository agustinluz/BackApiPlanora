	package com.ejemplos.DTO.Gasto;
	
	import com.ejemplos.DTO.Evento.EventoDTOConverter;
	import com.ejemplos.DTO.Grupo.GrupoDTOConverter;
	import com.ejemplos.DTO.Usuario.UsuarioDTOConverter;
	import com.ejemplos.modelo.Gasto;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.stereotype.Component;
	import java.util.stream.Collectors;
	
	@Component
	public class GastoDTOConverter {
	    
	    @Autowired
	    private UsuarioDTOConverter usuarioDTOConverter;
	    
	    @Autowired
	    private GrupoDTOConverter grupoDTOConverter;
	    
	    @Autowired
	    private EventoDTOConverter eventoDTOConverter;
	    
	    @Autowired
	    private DeudaGastoDTOConverter deudaGastoDTOConverter;
	    
	    public GastoDTO convertToDTO(Gasto gasto) {
	        if (gasto == null) return null;
	        
	        GastoDTO dto = new GastoDTO();
	        dto.setId(gasto.getId());
	        dto.setTitulo(gasto.getTitulo());
	        dto.setMonto(gasto.getMonto());
	        dto.setPartesIguales(gasto.isPartesIguales());
	        dto.setCantidadesPersonalizadas(gasto.getCantidadesPersonalizadas());
	        dto.setFechaCreacion(gasto.getFechaCreacion());

	        
	        if (gasto.getPagadoPor() != null) {
	            dto.setPagadoPor(usuarioDTOConverter.convertToDTO(gasto.getPagadoPor()));
	        }
	        
	        if (gasto.getGrupo() != null) {
	            dto.setGrupo(grupoDTOConverter.convertToDTO(gasto.getGrupo()));
	        }
	        
	        if (gasto.getEvento() != null) {
	            dto.setEvento(eventoDTOConverter.convertToDTO(gasto.getEvento()));
	        }
	        
	        if (gasto.getUsuarios() != null) {
	            dto.setUsuarios(gasto.getUsuarios().stream()
	                .map(usuarioDTOConverter::convertToDTO)
	                .collect(Collectors.toList()));
	        }
	        
	        if (gasto.getDeudas() != null) {
	            dto.setDeudas(gasto.getDeudas().stream()
	                .map(deudaGastoDTOConverter::convertToDTO)
	                .collect(Collectors.toList()));
	        }
	        
	        return dto;
	    }
	}