package com.focusup.controlador;

import com.focusup.dto.AuthResponse;
import com.focusup.dto.LoginRequest;
import com.focusup.dto.RegistroRequest;
import com.focusup.servicio.AuthServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del servicio web de autenticación (registro e inicio
 * de sesión).
 *
 * Evidencia GA7-220501096-AA5-EV01 — Diseño y desarrollo de servicios web.
 *
 * A diferencia de {@code TareaControlador} (que es {@code @Controller} y
 * devuelve vistas HTML con Thymeleaf), esta clase es {@code @RestController}:
 * cada método devuelve directamente un objeto que Spring convierte a JSON
 * en el cuerpo de la respuesta HTTP. Por eso este es el controlador
 * correcto para un "servicio web" que se prueba con Postman, no con un
 * navegador.
 *
 * Rutas disponibles (todas reciben el body en JSON, método POST):
 * POST /focusup/api/auth/registro
 * POST /focusup/api/auth/login
 *
 * (El prefijo /focusup viene de server.servlet.context-path en
 * application.properties, igual que en el resto del proyecto).
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthControlador {

    private final AuthServicio authServicio;

    public AuthControlador(AuthServicio authServicio) {
        this.authServicio = authServicio;
    }

    /**
     * Registra un usuario nuevo.
     *
     * Body esperado (JSON):
     * { "email": "correo@ejemplo.com", "password": "miClave123" }
     *
     * Respuestas:
     * - 201 Created si el registro fue exitoso.
     * - 409 Conflict si el correo ya estaba registrado.
     * - 400 Bad Request si el body no cumple las validaciones
     *   (correo vacío/mal formado, contraseña muy corta) — este caso lo
     *   maneja Spring automáticamente gracias a @Valid, antes de que el
     *   método siquiera se ejecute.
     */
    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        AuthResponse respuesta = authServicio.registrar(request);

        if (respuesta.isAutenticado()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        }
        // El correo ya existía: es un conflicto con el estado actual del servidor.
        return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
    }

    /**
     * Intenta iniciar sesión con un correo y una contraseña.
     *
     * Body esperado (JSON):
     * { "email": "correo@ejemplo.com", "password": "miClave123" }
     *
     * Respuestas:
     * - 200 OK si la autenticación fue satisfactoria.
     * - 401 Unauthorized si el correo no existe o la contraseña no coincide.
     *
     * Nota de seguridad: a propósito se devuelve el mismo mensaje de error
     * tanto si el correo no existe como si la contraseña es incorrecta.
     * Así no se le revela a quien está probando el API si un correo
     * específico está registrado o no.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse respuesta = authServicio.login(request);

        if (respuesta.isAutenticado()) {
            return ResponseEntity.ok(respuesta);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(respuesta);
    }
}
