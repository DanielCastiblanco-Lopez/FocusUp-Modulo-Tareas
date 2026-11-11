package com.focusup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Datos personales de un usuario. Lo usa el formulario "Mi cuenta" y es la
 * base de los formularios de registro y de edición del administrador.
 *
 * Reglas heredadas del módulo web de usuarios: nombres y apellidos solo con
 * letras, cédula solo con dígitos (mínimo 6), correo con formato válido.
 *
 * Por diseño NO incluye rol ni estado: un usuario normal nunca puede
 * enviarlos, así que tampoco puede cambiárselos aunque manipule la petición.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
public class PerfilForm {

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
    @Pattern(regexp = "^[\\p{L} .'-]*$", message = "Los nombres solo pueden contener letras")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
    @Pattern(regexp = "^[\\p{L} .'-]*$", message = "Los apellidos solo pueden contener letras")
    private String apellidos;

    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^\\d{6,15}$", message = "La cédula debe tener entre 6 y 15 dígitos")
    private String cedula;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String email;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
}
