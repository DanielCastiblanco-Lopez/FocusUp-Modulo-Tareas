package com.focusup.seguridad;

import com.focusup.modelo.Usuario;
import com.focusup.repositorio.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Exige que haya un usuario autenticado y activo para entrar a las rutas protegidas.
 *
 * Es la continuación, a nivel de toda la aplicación, de lo que antes hacía
 * UsuarioServlet en el módulo web: "si no hay usuario en sesión, al login".
 *
 * - Páginas web: redirige a /login.
 * - API (/api/...): responde 401 con un JSON.
 *
 * Deja el usuario verificado en el atributo {@link Sesion#ATRIBUTO_USUARIO_ACTUAL}
 * de la petición, para que los controladores lo usen. Los controladores nunca
 * confían en un id que venga del navegador: usan siempre este usuario.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
public class AutenticacionInterceptor implements HandlerInterceptor {

    private final UsuarioRepository usuarioRepository;
    private final boolean api;

    /**
     * @param api true para rutas de la API (responde 401 JSON), false para páginas (redirige al login)
     */
    public AutenticacionInterceptor(UsuarioRepository usuarioRepository, boolean api) {
        this.usuarioRepository = usuarioRepository;
        this.api = api;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        Long id = Sesion.usuarioId(request);
        Optional<Usuario> usuario = id == null
                ? Optional.empty()
                : usuarioRepository.findById(id).filter(Usuario::isActivo);

        if (usuario.isPresent()) {
            request.setAttribute(Sesion.ATRIBUTO_USUARIO_ACTUAL, usuario.get());
            return true;
        }

        // Sesión inexistente, cuenta eliminada o desactivada: se descarta la sesión.
        Sesion.cerrar(request);
        if (api) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"mensaje\":\"No autenticado: inicia sesión en /api/auth/login\"}");
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
        return false;
    }
}
