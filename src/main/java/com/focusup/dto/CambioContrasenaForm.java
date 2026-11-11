package com.focusup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cambio de contraseña. El usuario normal debe escribir también la
 * contraseña actual ({@code actual}); cuando es un administrador quien
 * restablece la clave de otra persona, ese campo no se usa.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
public class CambioContrasenaForm {

    private String actual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres")
    private String nueva;

    @NotBlank(message = "Confirma la nueva contraseña")
    private String confirmar;

    public String getActual() { return actual; }
    public void setActual(String actual) { this.actual = actual; }

    public String getNueva() { return nueva; }
    public void setNueva(String nueva) { this.nueva = nueva; }

    public String getConfirmar() { return confirmar; }
    public void setConfirmar(String confirmar) { this.confirmar = confirmar; }
}
