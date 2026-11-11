package com.focusup.excepcion;

/**
 * Se lanza cuando una operación incumple una regla de negocio (correo
 * repetido, intentar desactivar al último administrador, etc.). El
 * mensaje está escrito para mostrarse tal cual al usuario.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
