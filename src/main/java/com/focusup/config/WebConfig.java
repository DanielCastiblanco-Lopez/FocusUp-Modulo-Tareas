package com.focusup.config;

import com.focusup.repositorio.UsuarioRepository;
import com.focusup.seguridad.AdminInterceptor;
import com.focusup.seguridad.AutenticacionInterceptor;
import com.focusup.seguridad.CsrfInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra los interceptores de seguridad. El ORDEN importa:
 * 1) CSRF, 2) autenticación, 3) rol de administrador.
 *
 * Mapa de acceso:
 * <pre>
 *   /login, /registro, /css/**   público
 *   /tareas/**, /cuenta/**       usuario autenticado (cualquier rol)
 *   /usuarios/**                 solo ADMIN
 *   /api/auth/**                 público (login y registro de la API)
 *   /api/tareas/**               usuario autenticado
 * </pre>
 *
 * Se eligió una solución propia y pequeña en lugar de Spring Security
 * completo, igual que el proyecto ya hacía con BCrypt: así el flujo se
 * puede explicar línea por línea. Spring Security sería el siguiente paso.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UsuarioRepository usuarioRepository;

    public WebConfig(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(new CsrfInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/api/**", "/css/**", "/error");

        registro.addInterceptor(new AutenticacionInterceptor(usuarioRepository, false))
                .addPathPatterns("/tareas/**", "/cuenta/**", "/usuarios/**");

        registro.addInterceptor(new AutenticacionInterceptor(usuarioRepository, true))
                .addPathPatterns("/api/tareas/**");

        registro.addInterceptor(new AdminInterceptor())
                .addPathPatterns("/usuarios/**");
    }
}
