package com.focusup.dto;

import com.focusup.modelo.Tarea;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de salida que representa una tarea en las respuestas JSON del API.
 *
 * Se usa un DTO separado de la entidad para controlar exactamente qué
 * campos viajan al cliente (por ejemplo, evita exponer detalles internos
 * de Hibernate si la entidad llegara a crecer).
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
public class TareaResponse {

    private Long id;
    private String titulo;
    private String prioridad;
    private LocalDate fechaLimite;
    private String nota;
    private boolean completada;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCompletada;

    public TareaResponse() {
    }

    /** Construye el DTO de salida a partir de la entidad JPA. */
    public static TareaResponse desde(Tarea tarea) {
        TareaResponse respuesta = new TareaResponse();
        respuesta.id = tarea.getId();
        respuesta.titulo = tarea.getTitulo();
        respuesta.prioridad = tarea.getPrioridad();
        respuesta.fechaLimite = tarea.getFechaLimite();
        respuesta.nota = tarea.getNota();
        respuesta.completada = tarea.isCompletada();
        respuesta.fechaCreacion = tarea.getFechaCreacion();
        respuesta.fechaCompletada = tarea.getFechaCompletada();
        return respuesta;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public String getNota() {
        return nota;
    }

    public boolean isCompletada() {
        return completada;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaCompletada() {
        return fechaCompletada;
    }
}
