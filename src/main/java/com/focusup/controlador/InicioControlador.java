package com.focusup.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Página de inicio: lleva a la pantalla de tareas. Si no hay sesión, el
 * interceptor de autenticación la redirige al login.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@Controller
public class InicioControlador {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/tareas";
    }
}
