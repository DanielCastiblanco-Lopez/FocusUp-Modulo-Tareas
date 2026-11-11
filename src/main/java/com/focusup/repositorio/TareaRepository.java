package com.focusup.repositorio;

import com.focusup.modelo.Tarea;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link Tarea}.
 *
 * Desde la integración de módulos, todas las consultas se hacen "por
 * usuario": así es imposible, desde el servicio, leer o tocar tareas de
 * otra persona.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Tarea> findByUsuarioIdAndCompletadaFalseOrderByFechaCreacionDesc(Long usuarioId);

    List<Tarea> findByUsuarioIdAndCompletadaTrueOrderByFechaCompletadaDesc(Long usuarioId);

    Optional<Tarea> findByIdAndUsuarioId(Long id, Long usuarioId);

    long countByUsuarioIdAndCompletada(Long usuarioId, boolean completada);

    /** Tareas anteriores a la integración, que todavía no tienen dueño. */
    List<Tarea> findByUsuarioIdIsNull();
}
