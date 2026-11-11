package com.focusup.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una cuenta de usuario de FocusUp.
 *
 * Versión 2.0 (integración de módulos): une la entidad de autenticación
 * del módulo de tareas (correo + contraseña) con los datos personales que
 * manejaba el módulo web de usuarios (nombres, apellidos, cédula, fecha de
 * nacimiento, estado activo) y agrega el {@link Rol}.
 *
 * Las columnas nuevas son opcionales a nivel de base de datos a propósito:
 * así {@code ddl-auto=update} puede agregarlas a una tabla que ya tiene
 * usuarios (creados antes de la integración) sin fallar. Los getters
 * normalizan los valores vacíos: rol {@code USUARIO} y activo {@code true}.
 * Las reglas de obligatoriedad se validan en los formularios, no aquí.
 *
 * La contraseña se guarda siempre con hash BCrypt (60 caracteres, con sal
 * incluida). Nunca en texto plano.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Correo: es el nombre de usuario para iniciar sesión, por eso es único. */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Hash BCrypt de la contraseña. */
    @Column(nullable = false, length = 60)
    private String password;

    @Column(length = 100)
    private String nombres;

    @Column(length = 100)
    private String apellidos;

    /** Cédula: única entre usuarios. Puede ser nula en cuentas anteriores a la integración. */
    @Column(unique = true, length = 20)
    private String cedula;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    /** Baja lógica: un usuario inactivo no puede iniciar sesión, pero sus datos se conservan. */
    private Boolean activo = Boolean.TRUE;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Rol rol = Rol.USUARIO;

    public Usuario() {
    }

    public Usuario(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    /** Un valor nulo (cuenta anterior a la integración) cuenta como activo. */
    public boolean isActivo() { return activo == null || activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /** Un valor nulo (cuenta anterior a la integración) cuenta como USUARIO. */
    public Rol getRol() { return rol == null ? Rol.USUARIO : rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean esAdmin() { return getRol() == Rol.ADMIN; }

    /** Nombre para mostrar: "Nombres Apellidos", o el correo si aún no completó su perfil. */
    public String getNombreCompleto() {
        String completo = ((nombres == null ? "" : nombres) + " " + (apellidos == null ? "" : apellidos)).trim();
        return completo.isEmpty() ? email : completo;
    }

    @Override
    public String toString() {
        // Nunca se incluye la contraseña, ni siquiera el hash, para que no aparezca en un log.
        return "Usuario{id=" + id + ", email='" + email + "', rol=" + getRol() + "}";
    }
}
