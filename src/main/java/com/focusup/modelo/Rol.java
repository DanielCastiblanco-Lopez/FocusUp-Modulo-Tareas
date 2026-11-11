package com.focusup.modelo;

/**
 * Roles del sistema.
 *
 * <ul>
 *   <li>{@link #ADMIN}: puede ver, editar, activar y desactivar a todos los usuarios.</li>
 *   <li>{@link #USUARIO}: solo puede ver y editar su propia información y sus propias tareas.</li>
 * </ul>
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
public enum Rol {
    ADMIN("Administrador"),
    USUARIO("Usuario");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto para mostrar en las pantallas. */
    public String getEtiqueta() {
        return etiqueta;
    }
}
