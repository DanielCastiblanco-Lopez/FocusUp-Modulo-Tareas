package com.focusup.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.focusup.dto.AuthResponse;
import com.focusup.dto.LoginRequest;
import com.focusup.dto.RegistroForm;
import com.focusup.dto.RegistroRequest;
import com.focusup.excepcion.ReglaNegocioException;
import com.focusup.modelo.Rol;
import com.focusup.modelo.Usuario;
import com.focusup.repositorio.UsuarioRepository;
import java.time.LocalDate;
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

    // ── Integración de módulos: cuentas desactivadas y registro completo ──

    @Test
    void login_deUsuarioDesactivado_devuelveError() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Usuario desactivado = new Usuario("baja@focusup.com", encoder.encode("claveCorrecta"));
        desactivado.setActivo(false);

        when(usuarioRepository.findByEmail("baja@focusup.com")).thenReturn(Optional.of(desactivado));

        LoginRequest request = new LoginRequest();
        request.setEmail("baja@focusup.com");
        request.setPassword("claveCorrecta");

        assertFalse(authServicio.login(request).isAutenticado());
    }

    @Test
    void autenticar_normalizaElCorreo() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Usuario usuario = new Usuario("ana@focusup.com", encoder.encode("clave123"));
        when(usuarioRepository.findByEmail("ana@focusup.com")).thenReturn(Optional.of(usuario));

        assertTrue(authServicio.autenticar("  ANA@focusup.com ", "clave123").isPresent());
    }

    private RegistroForm formularioValido() {
        RegistroForm form = new RegistroForm();
        form.setNombres("Ana");
        form.setApellidos("Pérez");
        form.setCedula("1234567");
        form.setEmail("Ana@FocusUp.com");
        form.setFechaNacimiento(LocalDate.of(2000, 1, 15));
        form.setPassword("clave123");
        form.setConfirmar("clave123");
        return form;
    }

    @Test
    void registrarUsuario_siempreCreaRolUsuarioYGuardaHash() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario creado = authServicio.registrarUsuario(formularioValido());

        assertEquals(Rol.USUARIO, creado.getRol());
        assertEquals("ana@focusup.com", creado.getEmail());
        assertTrue(new BCryptPasswordEncoder().matches("clave123", creado.getPassword()));
    }

    @Test
    void registrarUsuario_conContrasenasDistintas_lanzaExcepcion() {
        RegistroForm form = formularioValido();
        form.setConfirmar("otraClave");

        assertThrows(ReglaNegocioException.class, () -> authServicio.registrarUsuario(form));
    }

    @Test
    void registrarUsuario_conCedulaRepetida_lanzaExcepcion() {
        when(usuarioRepository.existsByCedula("1234567")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> authServicio.registrarUsuario(formularioValido()));
    }
}
