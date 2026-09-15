package com.focusup.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entidad JPA que representa una cuenta de usuario del servicio de
 * autenticación (registro e inicio de sesión).
 *
 * Evidencia GA7-220501096-AA5-EV01 — Diseño y desarrollo de servicios web.
 *
 * La tabla {@code usuarios} se crea automáticamente gracias a
 * {@code spring.jpa.hibernate.ddl-auto=update}, igual que la tabla
 * {@code tareas} del módulo de tareas.
 *
 * IMPORTANTE: el campo {@code password} nunca guarda la contraseña en
 * texto plano. Se guarda su hash generado con BCrypt (ver
 * {@link com.focusup.servicio.AuthServicio}), que ya incluye su propia
 * sal aleatoria por dentro del hash — por eso no se necesita una columna
 * de "salt" aparte, a diferencia de un hash simple tipo SHA-256.
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    // ── Clave primaria ──────────────────────────────────────────────────

    /** Identificador único generado automáticamente por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Campos obligatorios ─────────────────────────────────────────────

    /**
     * Correo del usuario. Se usa como nombre de usuario para iniciar
     * sesión, por eso debe ser único en la base de datos.
     */
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Hash de la contraseña (BCrypt), NUNCA la contraseña en texto plano.
     * BCrypt genera una cadena de 60 caracteres que ya incluye la sal.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false, length = 60)
    private String password;

    // ── Constructores ────────────────────────────────────────────────────

    /** Constructor vacío requerido por JPA/Hibernate. */
    public Usuario() {
    }

    public Usuario(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // ── Getters y Setters ───────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public String toString() {
        // Nunca se incluye el password en el toString, ni siquiera el hash,
        // para que no aparezca por accidente en un log.
        return "Usuario{id=" + id + ", email='" + email + "'}";
    }
}
