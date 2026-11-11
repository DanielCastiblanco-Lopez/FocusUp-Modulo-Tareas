package com.focusup.config;

import com.focusup.controlador.AuthWebControlador;
import com.focusup.controlador.CuentaControlador;
import com.focusup.controlador.InicioControlador;
import com.focusup.controlador.TareaControlador;
import com.focusup.controlador.UsuariosControlador;
import com.focusup.modelo.Usuario;
import com.focusup.seguridad.Sesion;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Pone en el modelo de todas las vistas web (no en la API JSON) dos datos que necesitan las plantillas:
 * el usuario actual (para el menú y para mostrar u ocultar "Usuarios") y el
 * token CSRF (para los formularios).
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@ControllerAdvice(assignableTypes = {
        AuthWebControlador.class, CuentaControlador.class, InicioControlador.class,
        TareaControlador.class, UsuariosControlador.class})
public class AtributosGlobales {

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual(HttpServletRequest request) {
        return Sesion.usuarioActual(request);
    }

    @ModelAttribute("csrf")
    public String csrf(HttpServletRequest request) {
        return Sesion.tokenCsrf(request.getSession(true));
    }
}
