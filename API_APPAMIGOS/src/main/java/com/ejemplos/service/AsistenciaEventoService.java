package com.ejemplos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ejemplos.modelo.AsistenciaEvento;
import com.ejemplos.modelo.AsistenciaEventoRepository;

@Service
public class AsistenciaEventoService {
    @Autowired
    private AsistenciaEventoRepository asistenciaEventoRepository;

    public Long contarAsistenciasPorUsuarioYGrupo(Long usuarioId, Long grupoId) {
        return asistenciaEventoRepository.countByUsuarioIdAndGrupoId(usuarioId, grupoId);
    }

    public AsistenciaEvento guardar(AsistenciaEvento asistencia) {
        return asistenciaEventoRepository.save(asistencia);
    }
}