package com.focusup.seguridad;

import com.focusup.modelo.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilidades de sesión compartidas por la web y la API.
 *
 * En la sesión HTTP solo se guarda el ID del usuario (nunca el objeto
 * completo ni la contraseña). En cada petición protegida, el interceptor
 * vuelve a cargar al usuario desde la base de datos: así, si un
 * administrador desactiva una cuenta o le cambia el rol, el efecto es
 * inmediato y no hay que esperar a que expire la sesión.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
public final class Sesion {

    /** Atributo de sesión con el id del usuario autenticado. */
    public static final String ATRIBUTO_USUARIO_ID = "usuarioId";

    /** Atributo de sesión con el token anti-CSRF. */
    public static final String ATRIBUTO_CSRF = "csrf";

    /** Atributo de la petición con el usuario ya verificado por el interceptor. */
    public static final String ATRIBUTO_USUARIO_ACTUAL = "usuarioActual";

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Sesion() {
    }

    /**
     * Abre la sesión de un usuario que acaba de autenticarse.
     *
     * Se cambia el id de la sesión (protección contra fijación de sesión) y
     * se genera un token CSRF nuevo.
     */
    public static void iniciar(HttpServletRequest request, Usuario usuario) {
        request.getSession(true);
        request.changeSessionId();
        HttpSession sesion = request.getSession();
        sesion.setAttribute(ATRIBUTO_USUARIO_ID, usuario.getId());
        sesion.setAttribute(ATRIBUTO_CSRF, nuevoToken());
    }

    /** Cierra la sesión actual, si existe. */
    public static void cerrar(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
    }

    /** Id del usuario guardado en la sesión, o null si no hay sesión iniciada. */
    public static Long usuarioId(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return null;
        }
        Object valor = sesion.getAttribute(ATRIBUTO_USUARIO_ID);
        return valor instanceof Long id ? id : null;
    }

    /** Usuario autenticado de esta petición (lo deja el interceptor), o null. */
    public static Usuario usuarioActual(HttpServletRequest request) {
        Object valor = request.getAttribute(ATRIBUTO_USUARIO_ACTUAL);
        return valor instanceof Usuario usuario ? usuario : null;
    }

    /** Token CSRF de la sesión; lo crea si todavía no existe. */
    public static String tokenCsrf(HttpSession sesion) {
        Object valor = sesion.getAttribute(ATRIBUTO_CSRF);
        if (valor instanceof String token) {
            return token;
        }
        String token = nuevoToken();
        sesion.setAttribute(ATRIBUTO_CSRF, token);
        return token;
    }

    private static String nuevoToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
