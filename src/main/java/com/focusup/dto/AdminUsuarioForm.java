package com.focusup.dto;

import com.focusup.modelo.Rol;
import jakarta.validation.constraints.NotNull;

/**
 * Formulario con el que un ADMINISTRADOR edita a cualquier usuario: los
 * datos personales más el rol y el estado. Solo lo reciben los
 * controladores bajo /usuarios, que están protegidos para administradores.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
public class AdminUsuarioForm extends PerfilForm {

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    private boolean activo = true;

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
