package com.focusup.dto;

/**
 * DTO de respuesta para los endpoints de autenticación.
 *
 * Se devuelve tanto en éxito como en error, cambiando únicamente el
 * valor de {@code autenticado} y el {@code mensaje}, y el código de
 * estado HTTP con el que responde el controlador (200 en éxito, 401 o
 * 409 en error, según el caso).
 *
 * Ejemplo en éxito:
 * { "autenticado": true,  "mensaje": "Autenticación satisfactoria" }
 *
 * Ejemplo en error:
 * { "autenticado": false, "mensaje": "Error en la autenticación" }
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
public class AuthResponse {

    private boolean autenticado;
    private String mensaje;

    public AuthResponse(boolean autenticado, String mensaje) {
        this.autenticado = autenticado;
        this.mensaje = mensaje;
    }

    public boolean isAutenticado() {
        return autenticado;
    }

    public void setAutenticado(boolean autenticado) {
        this.autenticado = autenticado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
