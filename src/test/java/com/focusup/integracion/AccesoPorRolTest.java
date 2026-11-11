package com.focusup.integracion;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.focusup.modelo.Rol;
import com.focusup.modelo.Tarea;
import com.focusup.modelo.Usuario;
import com.focusup.repositorio.TareaRepository;
import com.focusup.repositorio.UsuarioRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración del control de acceso: arrancan la aplicación
 * completa (con H2 en memoria) y hacen peticiones HTTP simuladas.
 *
 * Demuestran las reglas de la integración:
 * - sin sesión no se entra a tareas, cuenta ni usuarios;
 * - un usuario normal no puede ver ni editar a otros usuarios (403);
 * - un usuario normal no puede ascenderse a administrador;
 * - cada usuario solo ve y modifica sus propias tareas;
 * - los formularios exigen el token CSRF;
 * - una cuenta desactivada pierde el acceso de inmediato.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccesoPorRolTest {

    private static final String CLAVE = "clave123";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private TareaRepository tareas;

    private Usuario admin;
    private Usuario ana;
    private Usuario beto;

    @BeforeEach
    void prepararDatos() {
        tareas.deleteAll();
        usuarios.deleteAll();
        admin = crearUsuario("admin@test.com", "Admin", "1000001", Rol.ADMIN);
        ana = crearUsuario("ana@test.com", "Ana", "1000002", Rol.USUARIO);
        beto = crearUsuario("beto@test.com", "Beto", "1000003", Rol.USUARIO);
    }

    // ── Sin sesión ──────────────────────────────────────────────────────────

    @Test
    void sinSesion_lasPaginasProtegidasRedirigenAlLogin() throws Exception {
        for (String ruta : new String[] {"/tareas", "/cuenta", "/usuarios"}) {
            mvc.perform(get(ruta)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        }
    }

    @Test
    void sinSesion_laApiDeTareasResponde401() throws Exception {
        mvc.perform(get("/api/tareas")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginConClaveIncorrecta_noAbreSesion() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(get("/login").session(sesion)).andExpect(status().isOk());

        mvc.perform(post("/login").session(sesion)
                        .param("_csrf", token(sesion)).param("email", "ana@test.com").param("password", "mala"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Correo o contraseña incorrectos")));
    }

    @Test
    void loginSinTokenCsrf_esRechazado() throws Exception {
        mvc.perform(post("/login").param("email", "ana@test.com").param("password", CLAVE))
                .andExpect(status().isForbidden());
    }

    // ── Usuario normal vs administrador ─────────────────────────────────────

    @Test
    void usuarioNormal_noPuedeVerElListadoNiEditarAOtros() throws Exception {
        MockHttpSession sesion = iniciarSesion("ana@test.com");

        mvc.perform(get("/usuarios").session(sesion)).andExpect(status().isForbidden());
        mvc.perform(get("/usuarios/" + beto.getId() + "/editar").session(sesion)).andExpect(status().isForbidden());
        mvc.perform(post("/usuarios/" + beto.getId()).session(sesion).param("_csrf", token(sesion))
                        .param("nombres", "Hack").param("apellidos", "Hack").param("cedula", "9999999")
                        .param("email", "beto@test.com").param("fechaNacimiento", "1990-01-01")
                        .param("rol", "ADMIN").param("activo", "true"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/usuarios/" + beto.getId() + "/estado").session(sesion).param("_csrf", token(sesion))
                        .param("activo", "false"))
                .andExpect(status().isForbidden());

        assertTrue(usuarios.findById(beto.getId()).orElseThrow().isActivo());
    }

    @Test
    void usuarioNormal_enMiCuentaSoloVeSusPropiosDatos() throws Exception {
        MockHttpSession sesion = iniciarSesion("ana@test.com");

        mvc.perform(get("/cuenta").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ana@test.com")))
                .andExpect(content().string(not(containsString("beto@test.com"))))
                .andExpect(content().string(not(containsString("admin@test.com"))));
    }

    @Test
    void usuarioNormal_noVeElEnlaceDeUsuarios_peroElAdminSi() throws Exception {
        mvc.perform(get("/tareas").session(iniciarSesion("ana@test.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/usuarios"))));
        mvc.perform(get("/tareas").session(iniciarSesion("admin@test.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/usuarios")));
    }

    @Test
    void usuarioNormal_noPuedeAscenderseNiDesactivarseDesdeMiCuenta() throws Exception {
        MockHttpSession sesion = iniciarSesion("ana@test.com");

        // Aunque manipule la petición agregando rol y activo, el formulario de "Mi cuenta" los ignora.
        mvc.perform(post("/cuenta").session(sesion).param("_csrf", token(sesion))
                        .param("nombres", "Ana").param("apellidos", "Nueva").param("cedula", "1000002")
                        .param("email", "ana@test.com").param("fechaNacimiento", "1990-01-01")
                        .param("rol", "ADMIN").param("activo", "false"))
                .andExpect(status().is3xxRedirection());

        Usuario recargada = usuarios.findById(ana.getId()).orElseThrow();
        assertEquals(Rol.USUARIO, recargada.getRol());
        assertTrue(recargada.isActivo());
        assertEquals("Nueva", recargada.getApellidos());
    }

    @Test
    void admin_veTodosLosUsuarios() throws Exception {
        mvc.perform(get("/usuarios").session(iniciarSesion("admin@test.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ana@test.com")))
                .andExpect(content().string(containsString("beto@test.com")));
    }

    @Test
    void admin_puedeDesactivarAUnUsuario_yEseUsuarioPierdeElAcceso() throws Exception {
        MockHttpSession sesionAna = iniciarSesion("ana@test.com");
        MockHttpSession sesionAdmin = iniciarSesion("admin@test.com");

        mvc.perform(post("/usuarios/" + ana.getId() + "/estado").session(sesionAdmin)
                        .param("_csrf", token(sesionAdmin)).param("activo", "false"))
                .andExpect(status().is3xxRedirection());

        // La sesión que Ana ya tenía abierta deja de servir en la siguiente petición.
        mvc.perform(get("/tareas").session(sesionAna))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        // Y no puede volver a entrar.
        MockHttpSession nueva = new MockHttpSession();
        mvc.perform(get("/login").session(nueva));
        mvc.perform(post("/login").session(nueva).param("_csrf", token(nueva))
                        .param("email", "ana@test.com").param("password", CLAVE))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Correo o contraseña incorrectos")));
    }

    @Test
    void admin_noPuedeDesactivarseASiMismo() throws Exception {
        MockHttpSession sesion = iniciarSesion("admin@test.com");

        mvc.perform(post("/usuarios/" + admin.getId() + "/estado").session(sesion)
                        .param("_csrf", token(sesion)).param("activo", "false"))
                .andExpect(status().is3xxRedirection());

        assertTrue(usuarios.findById(admin.getId()).orElseThrow().isActivo());
    }

    // ── Tareas por dueño ────────────────────────────────────────────────────

    @Test
    void cadaUsuarioSoloVeSusTareas() throws Exception {
        crearTarea("Tarea de Ana", ana);
        crearTarea("Tarea de Beto", beto);

        mvc.perform(get("/tareas").session(iniciarSesion("ana@test.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tarea de Ana")))
                .andExpect(content().string(not(containsString("Tarea de Beto"))));
    }

    @Test
    void usuarioNoPuedeEditarNiEliminarTareasAjenas() throws Exception {
        Tarea deBeto = crearTarea("Tarea de Beto", beto);
        MockHttpSession sesion = iniciarSesion("ana@test.com");

        mvc.perform(get("/tareas/" + deBeto.getId() + "/editar").session(sesion))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/tareas"));
        mvc.perform(post("/tareas/" + deBeto.getId()).session(sesion).param("_csrf", token(sesion))
                        .param("titulo", "Robada").param("prioridad", "Alta"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/tareas/" + deBeto.getId() + "/eliminar").session(sesion).param("_csrf", token(sesion)))
                .andExpect(status().is3xxRedirection());

        Tarea recargada = tareas.findById(deBeto.getId()).orElseThrow();
        assertEquals("Tarea de Beto", recargada.getTitulo());
        assertEquals(beto.getId(), recargada.getUsuarioId());
    }

    @Test
    void tareaNueva_quedaAsignadaAlUsuarioDeLaSesion_yNoAlQueDiceElFormulario() throws Exception {
        MockHttpSession sesion = iniciarSesion("ana@test.com");

        mvc.perform(post("/tareas").session(sesion).param("_csrf", token(sesion))
                        .param("titulo", "Mi tarea").param("prioridad", "Alta")
                        .param("usuarioId", String.valueOf(beto.getId())))
                .andExpect(status().is3xxRedirection());

        assertEquals(1, tareas.findByUsuarioIdOrderByFechaCreacionDesc(ana.getId()).size());
        assertEquals(0, tareas.findByUsuarioIdOrderByFechaCreacionDesc(beto.getId()).size());
    }

    @Test
    void formularioDeTareas_sinTokenCsrf_esRechazado() throws Exception {
        MockHttpSession sesion = iniciarSesion("ana@test.com");

        mvc.perform(post("/tareas").session(sesion).param("titulo", "Sin token").param("prioridad", "Alta"))
                .andExpect(status().isForbidden());
    }

    // ── API con sesión ──────────────────────────────────────────────────────

    @Test
    void api_loginAbreSesion_yLasTareasSonSoloDelUsuario() throws Exception {
        crearTarea("Tarea de Ana", ana);
        crearTarea("Tarea de Beto", beto);
        MockHttpSession sesion = new MockHttpSession();

        mvc.perform(post("/api/auth/login").session(sesion).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@test.com\",\"password\":\"" + CLAVE + "\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/tareas").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tarea de Ana")))
                .andExpect(content().string(not(containsString("Tarea de Beto"))));
    }

    @Test
    void api_loginConClaveIncorrecta_responde401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@test.com\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ── Auxiliares ──────────────────────────────────────────────────────────

    private Usuario crearUsuario(String email, String nombre, String cedula, Rol rol) {
        Usuario u = new Usuario(email, new BCryptPasswordEncoder().encode(CLAVE));
        u.setNombres(nombre);
        u.setApellidos("Prueba");
        u.setCedula(cedula);
        u.setFechaNacimiento(LocalDate.of(1995, 5, 20));
        u.setRol(rol);
        u.setActivo(true);
        return usuarios.save(u);
    }

    private Tarea crearTarea(String titulo, Usuario duenio) {
        Tarea t = new Tarea(titulo, "Media");
        t.setUsuarioId(duenio.getId());
        return tareas.save(t);
    }

    /** Inicia sesión por la pantalla de login, igual que lo haría un navegador. */
    private MockHttpSession iniciarSesion(String email) throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(get("/login").session(sesion)).andExpect(status().isOk());
        mvc.perform(post("/login").session(sesion).param("_csrf", token(sesion))
                        .param("email", email).param("password", CLAVE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tareas"));
        return sesion;
    }

    private static String token(MockHttpSession sesion) {
        return (String) sesion.getAttribute("csrf");
    }
}
