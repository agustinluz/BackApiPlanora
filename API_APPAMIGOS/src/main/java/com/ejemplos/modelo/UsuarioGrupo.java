package com.ejemplos.modelo;


import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
@Data
@Entity
@Table(name = "usuarios_grupos")
public class UsuarioGrupo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    // Ejemplo de campo adicional
    private String rol; // opcional, por ejemplo: "admin", "miembro", etc.


}
