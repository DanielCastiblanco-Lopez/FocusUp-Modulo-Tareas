package com.focusup.dto;

import com.focusup.modelo.Tarea;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO que representa el cuerpo JSON que el cliente envía para crear
 * o actualizar una tarea a través de la API REST.
 *
 * Se usa un DTO separado de la entidad {@code Tarea} para que el API
 * reciba solo los campos que el cliente puede escribir (no expone
 * {@code id}, {@code fechaCreacion} ni {@code fechaCompletada}, que
 * los controla el propio servidor).
 *
 * Ejemplo de body (JSON):
 * {
 *   "titulo": "Estudiar para el examen de bases de datos",
 *   "prioridad": "Alta",
 *   "fechaLimite": "2026-10-20",
 *   "nota": "Repasar normalización"
 * }
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
public class TareaRequest {

    @NotBlank(message = "El título de la tarea es obligatorio")
    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    private String titulo;

    /** "Alta", "Media" o "Baja". Si no se envía, el servicio usa "Media" por defecto. */
    @Pattern(regexp = "Alta|Media|Baja", message = "La prioridad debe ser Alta, Media o Baja")
    private String prioridad;

    /** Fecha límite en formato ISO (yyyy-MM-dd). Opcional. */
    private LocalDate fechaLimite;

    @Size(max = 500, message = "La nota no puede superar 500 caracteres")
    private String nota;

    public TareaRequest() {
    }

    /** Construye el formulario de edición a partir de una tarea existente. */
    public static TareaRequest desde(Tarea tarea) {
        TareaRequest request = new TareaRequest();
        request.titulo = tarea.getTitulo();
        request.prioridad = tarea.getPrioridad();
        request.fechaLimite = tarea.getFechaLimite();
        request.nota = tarea.getNota();
        return request;
    }

    /**
     * Copia a la entidad SOLO los campos editables. El id, el dueño y las
     * fechas de control nunca se toman de lo que envía el cliente.
     */
    public void copiarA(Tarea tarea) {
        tarea.setTitulo(titulo);
        tarea.setPrioridad(prioridad != null ? prioridad : "Media");
        tarea.setFechaLimite(fechaLimite);
        tarea.setNota(nota);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
