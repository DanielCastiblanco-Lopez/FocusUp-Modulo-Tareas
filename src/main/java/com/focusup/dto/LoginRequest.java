package com.focusup.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO que representa el cuerpo JSON que el cliente envía al endpoint
 * de inicio de sesión.
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
public class LoginRequest {

    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public LoginRequest() {
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
