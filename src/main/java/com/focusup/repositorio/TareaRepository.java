package com.focusup.repositorio;

import com.focusup.modelo.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Tarea}.
 * Spring Data genera automáticamente la implementación en tiempo de ejecución.
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByCompletadaFalseOrderByFechaCreacionDesc();

    List<Tarea> findByCompletadaTrueOrderByFechaCompletadaDesc();

    List<Tarea> findAllByOrderByFechaCreacionDesc();

    List<Tarea> findByPrioridad(String prioridad);
}
