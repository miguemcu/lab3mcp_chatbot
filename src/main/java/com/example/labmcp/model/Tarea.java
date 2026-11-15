package com.example.labmcp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Entidad JPA que representa una tarea almacenada en la tabla {@code tarea}.
 *
 * <p>Es el objeto que las herramientas MCP devuelven a los clientes; Spring AI
 * lo serializa a JSON en la respuesta de {@code tools/call}.</p>
 */
@Entity
@Table(name = "tarea")
public class Tarea {

    /** Identificador autoincremental generado por MySQL. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Título descriptivo; obligatorio. */
    @NotBlank
    private String titulo;

    /** Fecha asociada a la tarea, en texto (p. ej. {@code 2026-11-20}). */
    private String fecha;

    /** {@code true} si la tarea ya se completó. */
    private Boolean completado;

    /** Versión para bloqueo optimista; la gestiona JPA automáticamente. */
    @Version
    private Long version;

    /** Constructor vacío requerido por JPA. */
    public Tarea() {}

    /**
     * Crea una tarea nueva (sin id; lo asigna la base de datos).
     *
     * @param titulo     título de la tarea
     * @param fecha      fecha asociada
     * @param completado estado inicial
     */
    public Tarea(String titulo, String fecha, Boolean completado) {
        this.titulo = titulo;
        this.fecha = fecha;
        this.completado = completado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Boolean getCompletado() { return completado; }
    public void setCompletado(Boolean completado) { this.completado = completado; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
