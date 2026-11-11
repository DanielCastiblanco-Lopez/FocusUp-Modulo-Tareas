package com.focusup.dto;

/**
 * DTO genérico para respuestas de error o confirmación simples del API
 * (por ejemplo, 404 Not Found o confirmación de eliminación), cuando no
 * hace falta devolver un objeto completo.
 *
 * Ejemplo:
 * { "mensaje": "Tarea no encontrada" }
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
public class MensajeResponse {

    private String mensaje;

    public MensajeResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
