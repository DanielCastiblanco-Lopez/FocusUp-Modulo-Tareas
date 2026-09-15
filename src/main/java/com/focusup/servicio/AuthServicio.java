package com.focusup.servicio;

import com.focusup.dto.AuthResponse;
import com.focusup.dto.LoginRequest;
import com.focusup.dto.RegistroRequest;
import com.focusup.modelo.Usuario;
import com.focusup.repositorio.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio con la lógica de negocio del registro e inicio de sesión.
 *
 * Evidencia GA7-220501096-AA5-EV01 — Diseño y desarrollo de servicios web.
 *
 * Se apoya en {@link BCryptPasswordEncoder} (de spring-security-crypto)
 * para el hashing de contraseñas: BCrypt genera una sal aleatoria por
 * cada contraseña automáticamente y la incluye dentro del propio hash,
 * así que nunca se guarda ni se compara una contraseña en texto plano.
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
@Service
public class AuthServicio {

    private final UsuarioRepository usuarioRepository;

    // BCryptPasswordEncoder es seguro para reutilizar la misma instancia
    // en toda la aplicación (no guarda estado propio entre llamadas).
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServicio(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra un usuario nuevo.
     *
     * @param request correo y contraseña en texto plano recibidos del cliente
     * @return respuesta indicando éxito, o error si el correo ya existe
     */
    public AuthResponse registrar(RegistroRequest request) {
        // Regla de negocio: no permitir dos cuentas con el mismo correo.
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(false, "Error en la autenticación: el correo ya está registrado");
        }

        // Se hashea la contraseña ANTES de guardarla; jamás se persiste en texto plano.
        String hashDeLaContrasena = passwordEncoder.encode(request.getPassword());

        Usuario nuevoUsuario = new Usuario(request.getEmail(), hashDeLaContrasena);
        usuarioRepository.save(nuevoUsuario);

        return new AuthResponse(true, "Autenticación satisfactoria: usuario registrado correctamente");
    }

    /**
     * Intenta iniciar sesión con un correo y una contraseña.
     *
     * @param request correo y contraseña en texto plano recibidos del cliente
     * @return respuesta de éxito si las credenciales son correctas, o de
     *         error si el correo no existe o la contraseña no coincide
     */
    public AuthResponse login(LoginRequest request) {
        return usuarioRepository.findByEmail(request.getEmail())
                // matches() compara la contraseña en texto plano contra el
                // hash guardado, recalculando el hash con la misma sal que
                // ya está codificada dentro de ese hash.
                .filter(usuario -> passwordEncoder.matches(request.getPassword(), usuario.getPassword()))
                .map(usuario -> new AuthResponse(true, "Autenticación satisfactoria"))
                .orElseGet(() -> new AuthResponse(false, "Error en la autenticación"));
    }
}
