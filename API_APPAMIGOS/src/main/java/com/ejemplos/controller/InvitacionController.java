package com.ejemplos.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ejemplos.DTO.Invitacion.InvitacionDTO;
import com.ejemplos.DTO.Invitacion.InvitacionDTOConverter;
import com.ejemplos.service.InvitacionService;

@RestController
@RequestMapping("/api/invitaciones")
public class InvitacionController {

    @Autowired
    private InvitacionService invitacionService;

    @Autowired
    private InvitacionDTOConverter invitacionDTOConverter;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<InvitacionDTO>> listarInvitaciones(@PathVariable Long usuarioId) {
        List<InvitacionDTO> dtos = invitacionService.obtenerPorUsuario(usuarioId)
                .stream()
                .map(invitacionDTOConverter::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/aceptar")
    public ResponseEntity<Void> aceptar(@PathVariable Long id) {
        boolean ok = invitacionService.aceptarInvitacion(id);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<Void> rechazar(@PathVariable Long id) {
        boolean ok = invitacionService.rechazarInvitacion(id);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}