package com.focusup.servicio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.focusup.dto.AuthResponse;
import com.focusup.dto.LoginRequest;
import com.focusup.dto.RegistroRequest;
import com.focusup.modelo.Usuario;
import com.focusup.repositorio.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Pruebas unitarias de {@link AuthServicio}.
 *
 * Se usa Mockito para simular {@link UsuarioRepository} (así la prueba
 * no necesita una base de datos real conectada) y así probar solo la
 * lógica de negocio: registro con correo duplicado, login correcto y
 * login con contraseña incorrecta.
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
class AuthServicioTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private AuthServicio authServicio;

    @BeforeEach
    void configurar() {
        MockitoAnnotations.openMocks(this);
        authServicio = new AuthServicio(usuarioRepository);
    }

    @Test
    void registrar_conCorreoNuevo_devuelveExito() {
        RegistroRequest request = new RegistroRequest();
        request.setEmail("nuevo@focusup.com");
        request.setPassword("claveSegura1");

        when(usuarioRepository.existsByEmail("nuevo@focusup.com")).thenReturn(false);

        AuthResponse respuesta = authServicio.registrar(request);

        assertTrue(respuesta.isAutenticado());
        // Verifica que sí se intento guardar el usuario en el repositorio.
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void registrar_conCorreoYaExistente_devuelveError() {
        RegistroRequest request = new RegistroRequest();
        request.setEmail("repetido@focusup.com");
        request.setPassword("claveSegura1");

        when(usuarioRepository.existsByEmail("repetido@focusup.com")).thenReturn(true);

        AuthResponse respuesta = authServicio.registrar(request);

        assertFalse(respuesta.isAutenticado());
    }

    @Test
    void login_conCredencialesCorrectas_devuelveExito() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Usuario usuarioGuardado = new Usuario("valido@focusup.com", encoder.encode("claveCorrecta"));

        when(usuarioRepository.findByEmail("valido@focusup.com"))
                .thenReturn(Optional.of(usuarioGuardado));

        LoginRequest request = new LoginRequest();
        request.setEmail("valido@focusup.com");
        request.setPassword("claveCorrecta");

        AuthResponse respuesta = authServicio.login(request);

        assertTrue(respuesta.isAutenticado());
    }

    @Test
    void login_conContrasenaIncorrecta_devuelveError() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Usuario usuarioGuardado = new Usuario("valido@focusup.com", encoder.encode("claveCorrecta"));

        when(usuarioRepository.findByEmail("valido@focusup.com"))
                .thenReturn(Optional.of(usuarioGuardado));

        LoginRequest request = new LoginRequest();
        request.setEmail("valido@focusup.com");
        request.setPassword("claveIncorrecta");

        AuthResponse respuesta = authServicio.login(request);

        assertFalse(respuesta.isAutenticado());
    }

    @Test
    void login_conCorreoQueNoExiste_devuelveError() {
        when(usuarioRepository.findByEmail("noexiste@focusup.com"))
                .thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("noexiste@focusup.com");
        request.setPassword("cualquiera");

        AuthResponse respuesta = authServicio.login(request);

        assertFalse(respuesta.isAutenticado());
    }
}
