package com.ejemplos.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "imagenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Imagen implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre", length = 255)
    private String nombre; // Nombre original del archivo
    
    @Column(name = "tipo_contenido", length = 100)
    private String tipoContenido; // image/jpeg, image/png, etc.
    
    @Column(name = "tamaño")
    private Long tamaño;
    
    @Lob
    @Column(name = "datos", columnDefinition = "LONGTEXT")
    private String datos; // Los datos de la imagen en Base64
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;
    
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
    
    // Constructor para consultas que no incluyen datos Base64 (optimización)
    public Imagen(Long id, String nombre, String tipoContenido, Long tamaño, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.tipoContenido = tipoContenido;
        this.tamaño = tamaño;
        this.fechaCreacion = fechaCreacion;
    }
    
    // Método helper para verificar si es una imagen válida
    public boolean esImagenValida() {
        return tipoContenido != null && tipoContenido.startsWith("image/");
    }
    
    // Método helper para obtener el tamaño en formato legible
    public String getTamañoFormateado() {
        if (tamaño == null) return "0 B";
        
        long bytes = tamaño;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    // Método helper para verificar si tiene datos Base64
    public boolean tieneDatos() {
        return datos != null && !datos.trim().isEmpty();
    }
    
    // Método para obtener la extensión del archivo
    public String getExtension() {
        if (nombre == null || !nombre.contains(".")) {
            return "";
        }
        return nombre.substring(nombre.lastIndexOf(".") + 1).toLowerCase();
    }
}