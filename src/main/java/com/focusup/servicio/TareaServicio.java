package com.focusup.servicio;

import com.focusup.modelo.Tarea;
import com.focusup.repositorio.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Capa de servicio para la gestión de tareas.
 * Contiene la lógica de negocio y actúa como intermediario
 * entre el controlador y el repositorio.
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
@Service
@Transactional
public class TareaServicio {

    private final TareaRepository tareaRepository;

    public TareaServicio(TareaRepository tareaRepository) {
        this.tareaRepository = tareaRepository;
    }

    public List<Tarea> obtenerTodas() {
        return tareaRepository.findAllByOrderByFechaCreacionDesc();
    }

    public List<Tarea> obtenerPendientes() {
        return tareaRepository.findByCompletadaFalseOrderByFechaCreacionDesc();
    }

    public List<Tarea> obtenerCompletadas() {
        return tareaRepository.findByCompletadaTrueOrderByFechaCompletadaDesc();
    }

    public Optional<Tarea> obtenerPorId(Long id) {
        return tareaRepository.findById(id);
    }

    public Tarea guardar(Tarea tarea) {
        return tareaRepository.save(tarea);
    }

    public Tarea actualizar(Tarea tarea) {
        return tareaRepository.save(tarea);
    }

    public void eliminar(Long id) {
        tareaRepository.deleteById(id);
    }

    /**
     * Cambia el estado de completado de una tarea.
     * Registra el timestamp de completado cuando se marca como hecha
     * y lo limpia cuando se desmarca.
     *
     * @param id identificador de la tarea
     * @return tarea actualizada, o vacío si no existe
     */
    public Optional<Tarea> cambiarEstado(Long id) {
        return tareaRepository.findById(id).map(tarea -> {
            tarea.setCompletada(!tarea.isCompletada());
            tarea.setFechaCompletada(tarea.isCompletada() ? LocalDateTime.now() : null);
            return tareaRepository.save(tarea);
        });
    }

    public long contarPendientes() {
        return tareaRepository.findByCompletadaFalseOrderByFechaCreacionDesc().size();
    }

    public long contarCompletadas() {
        return tareaRepository.findByCompletadaTrueOrderByFechaCompletadaDesc().size();
    }
}
