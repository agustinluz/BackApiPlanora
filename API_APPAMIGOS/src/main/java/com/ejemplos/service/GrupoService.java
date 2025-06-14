package com.ejemplos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ejemplos.modelo.GrupoRepository;

import jakarta.transaction.Transactional;

import com.ejemplos.modelo.Grupo;



@Service
public class GrupoService {
    @Autowired
    private GrupoRepository grupoRepository;

    public List<Grupo> listarTodos() {
        return grupoRepository.findAll();
    }

    public Optional<Grupo> obtenerPorId(Long id) {
        return grupoRepository.findById(id);
    }

    public Grupo crear(Grupo grupo) {
        return grupoRepository.save(grupo);
    }

    @Transactional
    public void eliminar(Long id) {
        grupoRepository.findById(id).ifPresent(grupoRepository::delete);
    }
    public Optional<Grupo> obtenerPorCodigoInvitacion(String codigo) {
        return grupoRepository.findByCodigoInvitacion(codigo);
    }

}
