package com.focusup.controlador;

import com.focusup.modelo.Tarea;
import com.focusup.servicio.TareaServicio;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador Spring MVC para el módulo de tareas de FocusUp.
 * Maneja las peticiones HTTP GET y POST del CRUD completo.
 *
 * Rutas disponibles:
 * GET  /tareas              → lista todas las tareas
 * GET  /tareas/nueva        → formulario de nueva tarea
 * POST /tareas/guardar      → crea o actualiza una tarea
 * GET  /tareas/editar/{id}  → formulario de edición
 * GET  /tareas/completar/{id} → cambia el estado de completado
 * GET  /tareas/eliminar/{id}  → elimina la tarea
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
@Controller
@RequestMapping("/tareas")
public class TareaControlador {

    private final TareaServicio tareaServicio;

    public TareaControlador(TareaServicio tareaServicio) {
        this.tareaServicio = tareaServicio;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String filtro, Model model) {
        var tareas = switch (filtro != null ? filtro : "todas") {
            case "pendientes"  -> tareaServicio.obtenerPendientes();
            case "completadas" -> tareaServicio.obtenerCompletadas();
            default            -> tareaServicio.obtenerTodas();
        };
        model.addAttribute("tareas",          tareas);
        model.addAttribute("filtroActual",    filtro != null ? filtro : "todas");
        model.addAttribute("totalPendientes", tareaServicio.contarPendientes());
        model.addAttribute("totalCompletadas",tareaServicio.contarCompletadas());
        return "tareas/listar";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("tarea",  new Tarea());
        model.addAttribute("titulo", "Nueva tarea");
        return "tareas/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model,
                         RedirectAttributes redirect) {
        return tareaServicio.obtenerPorId(id).map(tarea -> {
            model.addAttribute("tarea",  tarea);
            model.addAttribute("titulo", "Editar tarea");
            return "tareas/formulario";
        }).orElseGet(() -> {
            redirect.addFlashAttribute("error", "Tarea no encontrada.");
            return "redirect:/tareas";
        });
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("tarea") Tarea tarea,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("titulo", tarea.getId() == null ? "Nueva tarea" : "Editar tarea");
            return "tareas/formulario";
        }
        boolean esNueva = tarea.getId() == null;
        tareaServicio.guardar(tarea);
        redirect.addFlashAttribute("exito",
            esNueva ? "Tarea creada exitosamente." : "Tarea actualizada exitosamente.");
        return "redirect:/tareas";
    }

    @GetMapping("/completar/{id}")
    public String completar(@PathVariable Long id, RedirectAttributes redirect) {
        tareaServicio.cambiarEstado(id).ifPresentOrElse(
            t -> redirect.addFlashAttribute("exito",
                    t.isCompletada() ? "Tarea completada." : "Tarea marcada como pendiente."),
            () -> redirect.addFlashAttribute("error", "Tarea no encontrada.")
        );
        return "redirect:/tareas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        if (tareaServicio.obtenerPorId(id).isPresent()) {
            tareaServicio.eliminar(id);
            redirect.addFlashAttribute("exito", "Tarea eliminada correctamente.");
        } else {
            redirect.addFlashAttribute("error", "Tarea no encontrada.");
        }
        return "redirect:/tareas";
    }
}
