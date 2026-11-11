package com.focusup.seguridad;

import com.focusup.modelo.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Deja pasar solo a administradores. Se aplica a todas las rutas /usuarios/**
 * (listar, editar, activar, desactivar, restablecer contraseñas).
 *
 * Va DESPUÉS de {@link AutenticacionInterceptor}, que ya dejó al usuario
 * verificado en la petición. Un usuario normal recibe 403 (Prohibido),
 * incluso si escribe la dirección a mano: ocultar el enlace en el menú es
 * solo comodidad; la protección real está aquí, en el servidor.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        Usuario usuario = Sesion.usuarioActual(request);
        if (usuario == null || !usuario.esAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
