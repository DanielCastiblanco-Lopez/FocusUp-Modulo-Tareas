package com.focusup.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Protección contra CSRF (Cross-Site Request Forgery) para los formularios web.
 *
 * Todo formulario POST lleva un campo oculto {@code _csrf} con un token
 * secreto guardado en la sesión. Una página maliciosa de otro sitio no
 * puede conocer ese token, así que no puede enviar un formulario válido a
 * nombre del usuario. Reemplaza los enlaces GET que cambiaban datos
 * (completar, eliminar, desactivar), que eran vulnerables.
 *
 * No se aplica a /api: ahí las peticiones son JSON y la cookie de sesión
 * es SameSite=Lax.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
public class CsrfInterceptor implements HandlerInterceptor {

    public static final String PARAMETRO = "_csrf";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession sesion = request.getSession(false);
        String esperado = sesion == null ? null : (String) sesion.getAttribute(Sesion.ATRIBUTO_CSRF);
        String recibido = request.getParameter(PARAMETRO);

        if (esperado == null || recibido == null || !iguales(esperado, recibido)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    /** Comparación en tiempo constante (no revela cuántos caracteres coinciden). */
    private static boolean iguales(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
