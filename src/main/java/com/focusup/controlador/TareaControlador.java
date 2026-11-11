package com.focusup.controlador;

import com.focusup.dto.TareaRequest;
import com.focusup.modelo.Usuario;
import com.focusup.seguridad.Sesion;
import com.focusup.servicio.TareaServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pantalla de tareas (vistas Thymeleaf). Es la pantalla principal después de iniciar sesión.
 *
 * Cada usuario trabaja SOLO con sus tareas: el dueño sale siempre de la
 * sesión, nunca del formulario. Las acciones que cambian datos son POST
 * con token CSRF (antes eran enlaces GET).
 *
 * Rutas:
 * GET  /tareas                    lista (filtro: todas | pendientes | completadas)
 * GET  /tareas/nueva              formulario de tarea nueva
 * POST /tareas                    crea la tarea
 * GET  /tareas/{id}/editar        formulario de edición
 * POST /tareas/{id}               guarda los cambios
 * POST /tareas/{id}/completar     alterna completada/pendiente
 * POST /tareas/{id}/eliminar      elimina la tarea
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@Controller
@RequestMapping("/tareas")
public class TareaControlador {

    private final TareaServicio tareaServicio;

    public TareaControlador(TareaServicio tareaServicio) {
        this.tareaServicio = tareaServicio;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String filtro,
                         HttpServletRequest request, Model model) {
        Long usuarioId = Sesion.usuarioActual(request).getId();
        String filtroActual = filtro != null ? filtro : "todas";

        var tareas = switch (filtroActual) {
            case "pendientes"  -> tareaServicio.obtenerPendientes(usuarioId);
            case "completadas" -> tareaServicio.obtenerCompletadas(usuarioId);
            default            -> tareaServicio.obtenerTodas(usuarioId);
        };
        model.addAttribute("tareas", tareas);
        model.addAttribute("filtroActual", filtroActual);
        model.addAttribute("totalPendientes", tareaServicio.contarPendientes(usuarioId));
        model.addAttribute("totalCompletadas", tareaServicio.contarCompletadas(usuarioId));
        return "tareas/listar";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        prepararFormulario(model, new TareaRequest(), null);
        return "tareas/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("tarea") TareaRequest tarea,
                        BindingResult resultado,
                        HttpServletRequest request, Model model,
                        RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("tareaId", null);
            model.addAttribute("titulo", "Nueva tarea");
            return "tareas/formulario";
        }
        Usuario usuario = Sesion.usuarioActual(request);
        var entidad = new com.focusup.modelo.Tarea();
        tarea.copiarA(entidad);
        tareaServicio.crear(entidad, usuario.getId());
        redirect.addFlashAttribute("exito", "Tarea creada exitosamente.");
        return "redirect:/tareas";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, HttpServletRequest request,
                         Model model, RedirectAttributes redirect) {
        Long usuarioId = Sesion.usuarioActual(request).getId();
        return tareaServicio.obtenerPorId(id, usuarioId).map(tarea -> {
            prepararFormulario(model, TareaRequest.desde(tarea), id);
            return "tareas/formulario";
        }).orElseGet(() -> {
            redirect.addFlashAttribute("error", "Tarea no encontrada.");
            return "redirect:/tareas";
        });
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("tarea") TareaRequest tarea,
                             BindingResult resultado,
                             HttpServletRequest request, Model model,
                             RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("tareaId", id);
            model.addAttribute("titulo", "Editar tarea");
            return "tareas/formulario";
        }
        Long usuarioId = Sesion.usuarioActual(request).getId();
        return tareaServicio.obtenerPorId(id, usuarioId).map(entidad -> {
            tarea.copiarA(entidad);
            tareaServicio.actualizar(entidad);
            redirect.addFlashAttribute("exito", "Tarea actualizada exitosamente.");
            return "redirect:/tareas";
        }).orElseGet(() -> {
            redirect.addFlashAttribute("error", "Tarea no encontrada.");
            return "redirect:/tareas";
        });
    }

    @PostMapping("/{id}/completar")
    public String completar(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
        Long usuarioId = Sesion.usuarioActual(request).getId();
        tareaServicio.cambiarEstado(id, usuarioId).ifPresentOrElse(
            t -> redirect.addFlashAttribute("exito",
                    t.isCompletada() ? "Tarea completada." : "Tarea marcada como pendiente."),
            () -> redirect.addFlashAttribute("error", "Tarea no encontrada.")
        );
        return "redirect:/tareas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
        Long usuarioId = Sesion.usuarioActual(request).getId();
        if (tareaServicio.eliminar(id, usuarioId)) {
            redirect.addFlashAttribute("exito", "Tarea eliminada correctamente.");
        } else {
            redirect.addFlashAttribute("error", "Tarea no encontrada.");
        }
        return "redirect:/tareas";
    }

    private void prepararFormulario(Model model, TareaRequest tarea, Long tareaId) {
        model.addAttribute("tarea", tarea);
        model.addAttribute("tareaId", tareaId);
        model.addAttribute("titulo", tareaId == null ? "Nueva tarea" : "Editar tarea");
    }
}
