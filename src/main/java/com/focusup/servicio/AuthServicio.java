package com.focusup.servicio;

import com.focusup.dto.AuthResponse;
import com.focusup.dto.LoginRequest;
import com.focusup.dto.RegistroForm;
import com.focusup.dto.RegistroRequest;
import com.focusup.excepcion.ReglaNegocioException;
import com.focusup.modelo.Rol;
import com.focusup.modelo.Usuario;
import com.focusup.repositorio.UsuarioRepository;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Lógica de registro e inicio de sesión (la usan tanto la web como la API).
 *
 * Usa BCrypt para las contraseñas: genera una sal aleatoria por contraseña
 * y la guarda dentro del propio hash. Esto reemplaza al MD5 sin sal que usaba
 * el módulo web original.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@Service
public class AuthServicio {

    private final UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServicio(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Deja el correo en minúsculas y sin espacios, para que "A@x.com" y "a@x.com" sean la misma cuenta. */
    static String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase();
    }

    /**
     * Registro desde la API (solo correo y contraseña). El perfil se
     * completa después en "Mi cuenta". El rol siempre es USUARIO.
     */
    public AuthResponse registrar(RegistroRequest request) {
        String email = normalizarCorreo(request.getEmail());
        if (usuarioRepository.existsByEmail(email)) {
            return new AuthResponse(false, "Error en la autenticación: el correo ya está registrado");
        }

        Usuario nuevoUsuario = new Usuario(email, passwordEncoder.encode(request.getPassword()));
        nuevoUsuario.setRol(Rol.USUARIO);
        usuarioRepository.save(nuevoUsuario);

        return new AuthResponse(true, "Autenticación satisfactoria: usuario registrado correctamente");
    }

    /**
     * Registro desde la web (formulario completo).
     *
     * @throws ReglaNegocioException si el correo o la cédula ya están registrados
     *                               o si las contraseñas no coinciden
     */
    public Usuario registrarUsuario(RegistroForm form) {
        if (!form.getPassword().equals(form.getConfirmar())) {
            throw new ReglaNegocioException("Las contraseñas no coinciden");
        }
        String email = normalizarCorreo(form.getEmail());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ReglaNegocioException("Ese correo ya está registrado");
        }
        if (usuarioRepository.existsByCedula(form.getCedula().trim())) {
            throw new ReglaNegocioException("Esa cédula ya está registrada");
        }

        Usuario usuario = new Usuario(email, passwordEncoder.encode(form.getPassword()));
        usuario.setNombres(form.getNombres().trim());
        usuario.setApellidos(form.getApellidos().trim());
        usuario.setCedula(form.getCedula().trim());
        usuario.setFechaNacimiento(form.getFechaNacimiento());
        usuario.setRol(Rol.USUARIO);   // nunca viene del formulario
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    /**
     * Comprueba correo y contraseña. Solo devuelve el usuario si la cuenta
     * existe, está activa y la contraseña coincide con el hash guardado.
     */
    public Optional<Usuario> autenticar(String correo, String contrasena) {
        if (correo == null || contrasena == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmail(normalizarCorreo(correo))
                .filter(Usuario::isActivo)
                .filter(usuario -> passwordEncoder.matches(contrasena, usuario.getPassword()));
    }

    /** Login de la API: mismo mensaje de error si el correo no existe o la clave es incorrecta. */
    public AuthResponse login(LoginRequest request) {
        return autenticar(request.getEmail(), request.getPassword())
                .map(usuario -> new AuthResponse(true, "Autenticación satisfactoria"))
                .orElseGet(() -> new AuthResponse(false, "Error en la autenticación"));
    }
}
