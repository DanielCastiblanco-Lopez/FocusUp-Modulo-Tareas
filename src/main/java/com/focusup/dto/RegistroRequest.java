package com.focusup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) que representa el cuerpo JSON que el
 * cliente envía al endpoint de registro.
 *
 * Se usa un DTO separado de la entidad {@code Usuario} a propósito:
 * así el API solo expone los campos que necesita recibir (correo y
 * contraseña en texto plano, tal como los escribe el usuario en el
 * formulario), y nunca expone directamente la entidad JPA ni el hash
 * guardado en base de datos.
 *
 * Ejemplo de body esperado (JSON):
 * {
 *   "email": "correo@ejemplo.com",
 *   "password": "miClave123"
 * }
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
public class RegistroRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    public RegistroRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
