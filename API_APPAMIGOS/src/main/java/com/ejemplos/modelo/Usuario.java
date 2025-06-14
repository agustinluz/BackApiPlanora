package com.ejemplos.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;
    private String nombre;
    
    @Lob
    @Column(name = "foto_perfil", columnDefinition = "LONGTEXT")
    private String fotoPerfil;
    
    @JsonIgnore
    private String password;

    @OneToMany(mappedBy = "pagadoPor")
    private List<Gasto> gastosPagados;

    @ManyToMany(mappedBy = "usuarios")
    private List<Gasto> gastosParticipados;

    @OneToMany(mappedBy = "usuario")
    private List<Imagen> imagenes;

    @OneToMany(mappedBy = "usuario")
    private List<Nota> notas;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsuarioGrupo> usuarioGrupos;
    

    @OneToMany(mappedBy = "usuario")
    private List<Voto> votos;
    
    @OneToMany(mappedBy = "deudor", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DeudaGasto> deudas; // Deudas que tiene este usuario

    @OneToMany(mappedBy = "acreedor", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DeudaGasto> creditos; // Dinero que le deben a este usuario

    @OneToMany(mappedBy = "creador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evento> eventosCreados;

    @ManyToMany
    @JoinTable(
        name = "evento_asistentes",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "evento_id")
    )
    private List<Evento> eventos = new ArrayList<>();
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventoAsistente> eventoAsistentes = new ArrayList<>();
    
    @OneToMany(mappedBy = "usuario")
    private List<AsistenciaEvento> asistenciasEventos;




}

