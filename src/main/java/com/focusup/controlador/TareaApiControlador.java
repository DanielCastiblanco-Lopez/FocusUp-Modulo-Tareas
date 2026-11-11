package com.focusup.controlador;

import com.focusup.dto.MensajeResponse;
import com.focusup.dto.TareaRequest;
import com.focusup.dto.TareaResponse;
import com.focusup.modelo.Tarea;
import com.focusup.seguridad.Sesion;
import jakarta.servlet.http.HttpServletRequest;
import com.focusup.servicio.TareaServicio;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST con el CRUD completo de tareas (API, responde JSON).
 *
 * Evidencia GA7-220501096-AA5-EV03 — Diseño y desarrollo de servicios web
 * (proyecto): expone como servicios web todas las operaciones que el
 * proyecto formativo FocusUp necesita sobre sus tareas.
 *
 * Este controlador es independiente de {@link TareaControlador}: aquel
 * es {@code @Controller} y sirve páginas HTML con Thymeleaf (para quien
 * usa la app desde el navegador); este es {@code @RestController} y
 * sirve JSON (para Postman, apps móviles, u otro cliente del API).
 * Ambos reutilizan la misma capa de negocio ({@link TareaServicio}),
 * así que la lógica de tareas no está duplicada en ningún lado.
 *
 * Integración de módulos: todas las rutas exigen sesión iniciada
 * (primero POST /api/auth/login; el cliente debe conservar la cookie
 * JSESSIONID, Postman lo hace solo). Sin sesión responden 401. Cada usuario
 * solo ve y modifica sus propias tareas; una tarea ajena responde 404.
 *
 * Rutas disponibles (todas bajo /focusup/api/tareas):
 * GET    /api/tareas                 → listar todas (o filtrar por ?filtro=pendientes|completadas)
 * GET    /api/tareas/{id}            → obtener una tarea por id
 * POST   /api/tareas                 → crear una tarea nueva
 * PUT    /api/tareas/{id}            → actualizar una tarea existente
 * PATCH  /api/tareas/{id}/completar  → alternar el estado completada/pendiente
 * DELETE /api/tareas/{id}            → eliminar una tarea
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
@RestController
@RequestMapping("/api/tareas")
public class TareaApiControlador {

    private final TareaServicio tareaServicio;

    public TareaApiControlador(TareaServicio tareaServicio) {
        this.tareaServicio = tareaServicio;
    }

    /**
     * Lista las tareas. Sin parámetros devuelve todas; con
     * {@code ?filtro=pendientes} o {@code ?filtro=completadas} filtra
     * el resultado — el mismo criterio que ya usaba la vista Thymeleaf.
     */
    @GetMapping
    public List<TareaResponse> listar(@RequestParam(required = false) String filtro, HttpServletRequest request) {
        Long usuarioId = Sesion.usuarioActual(request).getId();
        List<Tarea> tareas = switch (filtro != null ? filtro : "todas") {
            case "pendientes" -> tareaServicio.obtenerPendientes(usuarioId);
            case "completadas" -> tareaServicio.obtenerCompletadas(usuarioId);
            default -> tareaServicio.obtenerTodas(usuarioId);
        };
        return tareas.stream().map(TareaResponse::desde).toList();
    }

    /**
     * Obtiene una tarea puntual por su id.
     *
     * Respuestas: 200 OK con la tarea, o 404 Not Found si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id, HttpServletRequest request) {
        return tareaServicio.obtenerPorId(id, Sesion.usuarioActual(request).getId())
                .<ResponseEntity<Object>>map(tarea -> ResponseEntity.ok(TareaResponse.desde(tarea)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MensajeResponse("Tarea no encontrada")));
    }

    /**
     * Crea una tarea nueva.
     *
     * Body esperado: ver {@link TareaRequest}.
     * Respuestas: 201 Created con la tarea creada (incluyendo su id nuevo).
     */
    @PostMapping
    public ResponseEntity<TareaResponse> crear(@Valid @RequestBody TareaRequest request,
                                               HttpServletRequest peticion) {
        Tarea tarea = new Tarea();
        request.copiarA(tarea);

        // El dueño sale de la sesión, nunca del cuerpo de la petición.
        Tarea creada = tareaServicio.crear(tarea, Sesion.usuarioActual(peticion).getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TareaResponse.desde(creada));
    }

    /**
     * Actualiza una tarea existente (título, prioridad, fecha límite, nota).
     * El estado "completada" se maneja aparte, con el endpoint PATCH
     * /completar, porque es una acción distinta a editar los datos.
     *
     * Respuestas: 200 OK con la tarea actualizada, o 404 Not Found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody TareaRequest request,
                                        HttpServletRequest peticion) {
        return tareaServicio.obtenerPorId(id, Sesion.usuarioActual(peticion).getId())
                .<ResponseEntity<Object>>map(tarea -> {
                    request.copiarA(tarea);
                    Tarea actualizada = tareaServicio.actualizar(tarea);
                    return ResponseEntity.ok(TareaResponse.desde(actualizada));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MensajeResponse("Tarea no encontrada")));
    }

    /**
     * Alterna el estado de una tarea entre completada y pendiente
     * (reutiliza exactamente la misma lógica que ya usaba la vista web).
     *
     * Respuestas: 200 OK con la tarea actualizada, o 404 Not Found.
     */
    @PatchMapping("/{id}/completar")
    public ResponseEntity<?> completar(@PathVariable Long id, HttpServletRequest request) {
        return tareaServicio.cambiarEstado(id, Sesion.usuarioActual(request).getId())
                .<ResponseEntity<Object>>map(tarea -> ResponseEntity.ok(TareaResponse.desde(tarea)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MensajeResponse("Tarea no encontrada")));
    }

    /**
     * Elimina una tarea.
     *
     * Respuestas: 204 No Content si se eliminó, o 404 Not Found si no existía.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, HttpServletRequest request) {
        if (!tareaServicio.eliminar(id, Sesion.usuarioActual(request).getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensajeResponse("Tarea no encontrada"));
        }
        return ResponseEntity.noContent().build();
    }
}
