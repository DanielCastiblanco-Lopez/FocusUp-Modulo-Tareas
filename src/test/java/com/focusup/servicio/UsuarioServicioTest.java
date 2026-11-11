package com.focusup.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.focusup.dto.AdminUsuarioForm;
import com.focusup.dto.PerfilForm;
import com.focusup.excepcion.ReglaNegocioException;
import com.focusup.modelo.Rol;
import com.focusup.modelo.Usuario;
import com.focusup.repositorio.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Pruebas unitarias de las reglas de negocio de {@link UsuarioServicio}.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
class UsuarioServicioTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioServicio usuarioServicio;

    @BeforeEach
    void configurar() {
        MockitoAnnotations.openMocks(this);
        usuarioServicio = new UsuarioServicio(usuarioRepository);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Usuario usuario(long id, String email, Rol rol, boolean activo) {
        Usuario u = new Usuario(email, new BCryptPasswordEncoder().encode("clave123"));
        u.setId(id);
        u.setRol(rol);
        u.setActivo(activo);
        return u;
    }

    private PerfilForm perfil(String email, String cedula) {
        PerfilForm f = new PerfilForm();
        f.setNombres(" Ana ");
        f.setApellidos("Pérez");
        f.setCedula(cedula);
        f.setEmail(email);
        f.setFechaNacimiento(LocalDate.of(2000, 1, 15));
        return f;
    }

    private AdminUsuarioForm formAdmin(Rol rol, boolean activo) {
        AdminUsuarioForm f = new AdminUsuarioForm();
        f.setNombres("Ana");
        f.setApellidos("Pérez");
        f.setCedula("1234567");
        f.setEmail("ana@focusup.com");
        f.setFechaNacimiento(LocalDate.of(2000, 1, 15));
        f.setRol(rol);
        f.setActivo(activo);
        return f;
    }

    // ── Datos personales ────────────────────────────────────────────────────

    @Test
    void actualizarPerfil_guardaDatosLimpiosYCorreoEnMinusculas() {
        Usuario ana = usuario(1, "ana@focusup.com", Rol.USUARIO, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));

        Usuario resultado = usuarioServicio.actualizarPerfil(1L, perfil("ANA@focusup.com", "1234567"));

        assertEquals("Ana", resultado.getNombres());
        assertEquals("ana@focusup.com", resultado.getEmail());
    }

    @Test
    void actualizarPerfil_noCambiaRolNiEstado() {
        Usuario ana = usuario(1, "ana@focusup.com", Rol.USUARIO, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));

        usuarioServicio.actualizarPerfil(1L, perfil("ana@focusup.com", "1234567"));

        assertEquals(Rol.USUARIO, ana.getRol());
        assertTrue(ana.isActivo());
    }

    @Test
    void actualizarPerfil_conCorreoDeOtroUsuario_lanzaExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1, "ana@focusup.com", Rol.USUARIO, true)));
        when(usuarioRepository.existsByEmailAndIdNot("beto@focusup.com", 1L)).thenReturn(true);

        assertThrows(ReglaNegocioException.class,
                () -> usuarioServicio.actualizarPerfil(1L, perfil("beto@focusup.com", "1234567")));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarPerfil_conCedulaDeOtroUsuario_lanzaExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1, "ana@focusup.com", Rol.USUARIO, true)));
        when(usuarioRepository.existsByCedulaAndIdNot("7654321", 1L)).thenReturn(true);

        assertThrows(ReglaNegocioException.class,
                () -> usuarioServicio.actualizarPerfil(1L, perfil("ana@focusup.com", "7654321")));
    }

    // ── Reglas de administración ────────────────────────────────────────────

    @Test
    void admin_puedeCambiarElRolDeOtroUsuario() {
        Usuario admin = usuario(1, "admin@focusup.com", Rol.ADMIN, true);
        Usuario ana = usuario(2, "ana@focusup.com", Rol.USUARIO, true);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(ana));

        usuarioServicio.actualizarComoAdmin(admin.getId(), 2L, formAdmin(Rol.ADMIN, true));

        assertEquals(Rol.ADMIN, ana.getRol());
    }

    @Test
    void admin_noPuedeQuitarseSuPropioRol() {
        Usuario admin = usuario(1, "admin@focusup.com", Rol.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(ReglaNegocioException.class,
                () -> usuarioServicio.actualizarComoAdmin(1L, 1L, formAdmin(Rol.USUARIO, true)));
        assertEquals(Rol.ADMIN, admin.getRol());
    }

    @Test
    void admin_noPuedeDesactivarse() {
        Usuario admin = usuario(1, "admin@focusup.com", Rol.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(ReglaNegocioException.class, () -> usuarioServicio.cambiarEstado(1L, 1L, false));
        assertTrue(admin.isActivo());
    }

    @Test
    void noSePuedeDesactivarAlUltimoAdministradorActivo() {
        Usuario unicoAdmin = usuario(2, "admin2@focusup.com", Rol.ADMIN, true);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(unicoAdmin));
        when(usuarioRepository.findAll()).thenReturn(List.of(unicoAdmin));

        // Lo intenta alguien con id distinto (por ejemplo un admin que ya no está activo).
        assertThrows(ReglaNegocioException.class, () -> usuarioServicio.cambiarEstado(1L, 2L, false));
        assertTrue(unicoAdmin.isActivo());
    }

    @Test
    void sePuedeDesactivarAUnAdminSiHayOtroActivo() {
        Usuario admin1 = usuario(1, "admin1@focusup.com", Rol.ADMIN, true);
        Usuario admin2 = usuario(2, "admin2@focusup.com", Rol.ADMIN, true);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(admin2));
        when(usuarioRepository.findAll()).thenReturn(List.of(admin1, admin2));

        usuarioServicio.cambiarEstado(1L, 2L, false);

        assertFalse(admin2.isActivo());
    }

    @Test
    void admin_puedeDesactivarYReactivarAUnUsuarioNormal() {
        Usuario ana = usuario(2, "ana@focusup.com", Rol.USUARIO, true);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(ana));

        usuarioServicio.cambiarEstado(1L, 2L, false);
        assertFalse(ana.isActivo());

        usuarioServicio.cambiarEstado(1L, 2L, true);
        assertTrue(ana.isActivo());
    }

    // ── Contraseñas ─────────────────────────────────────────────────────────

    @Test
    void cambiarContrasenaPropia_conActualCorrecta_guardaNuevoHash() {
        Usuario ana = usuario(1, "ana@focusup.com", Rol.USUARIO, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));

        usuarioServicio.cambiarContrasenaPropia(1L, "clave123", "nuevaClave9", "nuevaClave9");

        assertTrue(new BCryptPasswordEncoder().matches("nuevaClave9", ana.getPassword()));
    }

    @Test
    void cambiarContrasenaPropia_conActualIncorrecta_lanzaExcepcion() {
        Usuario ana = usuario(1, "ana@focusup.com", Rol.USUARIO, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));

        assertThrows(ReglaNegocioException.class,
                () -> usuarioServicio.cambiarContrasenaPropia(1L, "equivocada", "nuevaClave9", "nuevaClave9"));
        assertTrue(new BCryptPasswordEncoder().matches("clave123", ana.getPassword()));
    }

    @Test
    void restablecerContrasena_conConfirmacionDistinta_lanzaExcepcion() {
        Usuario ana = usuario(1, "ana@focusup.com", Rol.USUARIO, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));

        assertThrows(ReglaNegocioException.class,
                () -> usuarioServicio.restablecerContrasena(1L, "nuevaClave9", "otraDistinta"));
    }

    @Test
    void restablecerContrasena_conClaveCorta_lanzaExcepcion() {
        Usuario ana = usuario(1, "ana@focusup.com", Rol.USUARIO, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));

        assertThrows(ReglaNegocioException.class,
                () -> usuarioServicio.restablecerContrasena(1L, "abc", "abc"));
    }

    @Test
    void obtener_deUsuarioInexistente_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class, () -> usuarioServicio.obtener(99L));
    }
}
