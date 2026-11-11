package com.focusup.servicio;

import com.focusup.modelo.Tarea;
import com.focusup.repositorio.TareaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio de las tareas.
 *
 * Desde la integración de módulos todos los métodos reciben el id del
 * usuario dueño y solo trabajan con SUS tareas. Si alguien pide una tarea
 * que existe pero es de otro usuario, el resultado es el mismo que si no
 * existiera (vacío), así no se revela que esa tarea existe.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@Service
@Transactional
public class TareaServicio {

    private final TareaRepository tareaRepository;

    public TareaServicio(TareaRepository tareaRepository) {
        this.tareaRepository = tareaRepository;
    }

    public List<Tarea> obtenerTodas(Long usuarioId) {
        return tareaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    public List<Tarea> obtenerPendientes(Long usuarioId) {
        return tareaRepository.findByUsuarioIdAndCompletadaFalseOrderByFechaCreacionDesc(usuarioId);
    }

    public List<Tarea> obtenerCompletadas(Long usuarioId) {
        return tareaRepository.findByUsuarioIdAndCompletadaTrueOrderByFechaCompletadaDesc(usuarioId);
    }

    public Optional<Tarea> obtenerPorId(Long id, Long usuarioId) {
        return tareaRepository.findByIdAndUsuarioId(id, usuarioId);
    }

    /** Guarda una tarea nueva asignándola al usuario indicado (el dueño nunca viene del cliente). */
    public Tarea crear(Tarea tarea, Long usuarioId) {
        tarea.setId(null);
        tarea.setUsuarioId(usuarioId);
        return tareaRepository.save(tarea);
    }

    /** Guarda los cambios de una tarea que ya fue cargada con {@link #obtenerPorId}. */
    public Tarea actualizar(Tarea tarea) {
        return tareaRepository.save(tarea);
    }

    /** @return true si la tarea existía y era del usuario (y se eliminó). */
    public boolean eliminar(Long id, Long usuarioId) {
        return tareaRepository.findByIdAndUsuarioId(id, usuarioId)
                .map(tarea -> {
                    tareaRepository.delete(tarea);
                    return true;
                })
                .orElse(false);
    }

    /** Alterna completada/pendiente y registra o limpia la fecha de completado. */
    public Optional<Tarea> cambiarEstado(Long id, Long usuarioId) {
        return tareaRepository.findByIdAndUsuarioId(id, usuarioId).map(tarea -> {
            tarea.setCompletada(!tarea.isCompletada());
            tarea.setFechaCompletada(tarea.isCompletada() ? LocalDateTime.now() : null);
            return tareaRepository.save(tarea);
        });
    }

    public long contarPendientes(Long usuarioId) {
        return tareaRepository.countByUsuarioIdAndCompletada(usuarioId, false);
    }

    public long contarCompletadas(Long usuarioId) {
        return tareaRepository.countByUsuarioIdAndCompletada(usuarioId, true);
    }
}
