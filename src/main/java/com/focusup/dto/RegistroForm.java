package com.focusup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formulario de registro de la web: datos personales + contraseña.
 * El rol no viaja en el formulario: toda cuenta nueva es USUARIO.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
public class RegistroForm extends PerfilForm {

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres")
    private String password;

    @NotBlank(message = "Confirma la contraseña")
    private String confirmar;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmar() { return confirmar; }
    public void setConfirmar(String confirmar) { this.confirmar = confirmar; }
}
