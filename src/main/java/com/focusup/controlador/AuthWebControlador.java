package com.focusup.controlador;

import com.focusup.dto.RegistroForm;
import com.focusup.excepcion.ReglaNegocioException;
import com.focusup.modelo.Usuario;
import com.focusup.seguridad.Sesion;
import com.focusup.servicio.AuthServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Pantallas de inicio de sesión, registro y cierre de sesión.
 *
 * Flujo: login o registro correcto → pantalla de tareas (/tareas).
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@Controller
public class AuthWebControlador {

    private static final String MENSAJE_LOGIN_FALLIDO = "Correo o contraseña incorrectos";

    private final AuthServicio authServicio;

    public AuthWebControlador(AuthServicio authServicio) {
        this.authServicio = authServicio;
    }

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(required = false) String salio,
                               HttpServletRequest request, Model model) {
        if (Sesion.usuarioId(request) != null) {
            return "redirect:/tareas";
        }
        model.addAttribute("salio", salio != null);
        return "auth/login";
    }

    @PostMapping("/login")
    public String iniciarSesion(@RequestParam(defaultValue = "") String email,
                                @RequestParam(defaultValue = "") String password,
                                HttpServletRequest request, Model model) {
        return authServicio.autenticar(email, password)
                .map(usuario -> {
                    Sesion.iniciar(request, usuario);
                    return "redirect:/tareas";
                })
                .orElseGet(() -> {
                    // Mismo mensaje si el correo no existe, la cuenta está desactivada o la clave falla.
                    model.addAttribute("error", MENSAJE_LOGIN_FALLIDO);
                    model.addAttribute("email", email);
                    return "auth/login";
                });
    }

    @GetMapping("/registro")
    public String mostrarRegistro(HttpServletRequest request, Model model) {
        if (Sesion.usuarioId(request) != null) {
            return "redirect:/tareas";
        }
        model.addAttribute("registro", new RegistroForm());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registro") RegistroForm form,
                            BindingResult resultado,
                            HttpServletRequest request, Model model) {
        if (resultado.hasErrors()) {
            return "auth/registro";
        }
        try {
            Usuario nuevo = authServicio.registrarUsuario(form);
            Sesion.iniciar(request, nuevo);   // tras registrarse entra directo a sus tareas
            return "redirect:/tareas";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/registro";
        }
    }

    /** Cerrar sesión es POST (con token CSRF) para que otra página no pueda cerrarla a escondidas. */
    @PostMapping("/logout")
    public String cerrarSesion(HttpServletRequest request) {
        Sesion.cerrar(request);
        return "redirect:/login?salio";
    }
}
