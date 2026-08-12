package com.focusup.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una tarea en el sistema FocusUp.
 *
 * La anotación {@code @Entity} indica a Hibernate que esta clase
 * corresponde a una tabla en la base de datos. La tabla se crea
 * automáticamente gracias a {@code spring.jpa.hibernate.ddl-auto=update}.
 *
 * Campos de la tabla {@code tareas}:
 * <ul>
 *   <li>{@code id}           → clave primaria autoincremental</li>
 *   <li>{@code titulo}       → texto descriptivo de la tarea (obligatorio)</li>
 *   <li>{@code prioridad}    → "Alta", "Media" o "Baja"</li>
 *   <li>{@code fechaLimite}  → fecha límite de la tarea</li>
 *   <li>{@code completada}   → estado de la tarea (false por defecto)</li>
 *   <li>{@code nota}         → contexto adicional (opcional)</li>
 *   <li>{@code fechaCreacion}→ timestamp de creación (auto)</li>
 *   <li>{@code fechaCompletada} → timestamp cuando se completó</li>
 * </ul>
 *
 * @author  Daniel Castiblanco
 * @version 1.0
 */
@Entity
@Table(name = "tareas")
public class Tarea {

    // ── Clave primaria ────────────────────────────────────────────────────────

    /**
     * Identificador único generado automáticamente por la base de datos.
     * Estrategia IDENTITY: usa AUTO_INCREMENT de MySQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Campos obligatorios ───────────────────────────────────────────────────

    /**
     * Título descriptivo de la tarea.
     * No puede estar vacío y tiene un máximo de 200 caracteres.
     */
    @NotBlank(message = "El título de la tarea es obligatorio")
    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    /**
     * Nivel de prioridad: "Alta", "Media" o "Baja".
     * Determina el color del badge en la interfaz.
     */
    @Column(nullable = false, length = 10)
    private String prioridad = "Media";

    // ── Campos opcionales ─────────────────────────────────────────────────────

    /** Fecha límite para completar la tarea. Puede ser null si no se define. */
    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    /**
     * Nota o contexto adicional sobre la tarea.
     * Campo opcional con un máximo de 500 caracteres.
     */
    @Size(max = 500, message = "La nota no puede superar 500 caracteres")
    @Column(length = 500)
    private String nota;

    // ── Campos de estado y auditoría ──────────────────────────────────────────

    /** Indica si la tarea fue completada. {@code false} por defecto. */
    @Column(nullable = false)
    private boolean completada = false;

    /**
     * Timestamp de creación de la tarea.
     * Se asigna automáticamente al persistir por primera vez.
     */
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Timestamp del momento en que se marcó como completada.
     * Valor {@code null} si aún está pendiente.
     */
    @Column(name = "fecha_completada")
    private LocalDateTime fechaCompletada;

    // ── Constructores ─────────────────────────────────────────────────────────

    /** Constructor vacío requerido por JPA/Hibernate. */
    public Tarea() {}

    /**
     * Constructor de conveniencia para crear una tarea nueva.
     *
     * @param titulo    título de la tarea
     * @param prioridad nivel de prioridad ("Alta", "Media", "Baja")
     */
    public Tarea(String titulo, String prioridad) {
        this.titulo    = titulo;
        this.prioridad = prioridad;
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }

    public String getTitulo()                   { return titulo; }
    public void setTitulo(String titulo)        { this.titulo = titulo; }

    public String getPrioridad()                { return prioridad; }
    public void setPrioridad(String prioridad)  { this.prioridad = prioridad; }

    public LocalDate getFechaLimite()           { return fechaLimite; }
    public void setFechaLimite(LocalDate f)     { this.fechaLimite = f; }

    public String getNota()                     { return nota; }
    public void setNota(String nota)            { this.nota = nota; }

    public boolean isCompletada()               { return completada; }
    public void setCompletada(boolean c)        { this.completada = c; }

    public LocalDateTime getFechaCreacion()     { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }

    public LocalDateTime getFechaCompletada()   { return fechaCompletada; }
    public void setFechaCompletada(LocalDateTime f) { this.fechaCompletada = f; }

    // ── Métodos de utilidad ───────────────────────────────────────────────────

    /**
     * Retorna el color CSS correspondiente al nivel de prioridad.
     * Usado en las plantillas Thymeleaf para colorear el badge.
     *
     * @return código de color hexadecimal
     */
    public String getColorPrioridad() {
        return switch (prioridad) {
            case "Alta"  -> "#E74C3C";
            case "Baja"  -> "#27AE60";
            default      -> "#F39C12"; // Media
        };
    }

    @Override
    public String toString() {
        return "Tarea{id=" + id + ", titulo='" + titulo + "', prioridad='" + prioridad + "', completada=" + completada + "}";
    }
}
