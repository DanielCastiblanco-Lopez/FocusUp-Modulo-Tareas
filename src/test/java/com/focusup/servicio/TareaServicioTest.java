package com.focusup.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.focusup.modelo.Tarea;
import com.focusup.repositorio.TareaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Pruebas unitarias de {@link TareaServicio} (con el repositorio simulado
 * con Mockito). Verifican, además de la lógica de estado, que cada
 * operación está limitada al usuario dueño de la tarea.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
class TareaServicioTest {

    private static final Long USUARIO = 7L;
    private static final Long OTRO_USUARIO = 8L;

    @Mock
    private TareaRepository tareaRepository;

    private TareaServicio tareaServicio;

    @BeforeEach
    void configurar() {
        MockitoAnnotations.openMocks(this);
        tareaServicio = new TareaServicio(tareaRepository);
    }

    @Test
    void crear_asignaLaTareaAlUsuarioIndicado() {
        Tarea tarea = new Tarea("Estudiar Spring", "Alta");
        tarea.setId(99L);               // un id enviado por el cliente debe ignorarse
        tarea.setUsuarioId(OTRO_USUARIO); // un dueño enviado por el cliente debe ignorarse
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        Tarea resultado = tareaServicio.crear(tarea, USUARIO);

        assertEquals(USUARIO, resultado.getUsuarioId());
        assertNull(resultado.getId());
    }

    @Test
    void obtenerPorId_buscaSiempreConElDuenio() {
        when(tareaRepository.findByIdAndUsuarioId(5L, OTRO_USUARIO)).thenReturn(Optional.empty());

        assertTrue(tareaServicio.obtenerPorId(5L, OTRO_USUARIO).isEmpty());
        verify(tareaRepository).findByIdAndUsuarioId(5L, OTRO_USUARIO);
    }

    @Test
    void cambiarEstado_deTareaPendienteACompletada_marcaFechaCompletada() {
        Tarea pendiente = new Tarea("Lavar el carro", "Baja");
        pendiente.setId(5L);
        pendiente.setCompletada(false);

        when(tareaRepository.findByIdAndUsuarioId(5L, USUARIO)).thenReturn(Optional.of(pendiente));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Tarea> resultado = tareaServicio.cambiarEstado(5L, USUARIO);

        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().isCompletada());
        assertNotNull(resultado.get().getFechaCompletada());
    }

    @Test
    void cambiarEstado_deTareaCompletadaAPendiente_limpiaFechaCompletada() {
        Tarea completada = new Tarea("Lavar el carro", "Baja");
        completada.setId(6L);
        completada.setCompletada(true);

        when(tareaRepository.findByIdAndUsuarioId(6L, USUARIO)).thenReturn(Optional.of(completada));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Tarea> resultado = tareaServicio.cambiarEstado(6L, USUARIO);

        assertTrue(resultado.isPresent());
        assertFalse(resultado.get().isCompletada());
        assertNull(resultado.get().getFechaCompletada());
    }

    @Test
    void cambiarEstado_deTareaAjena_devuelveVacioYNoGuarda() {
        when(tareaRepository.findByIdAndUsuarioId(5L, OTRO_USUARIO)).thenReturn(Optional.empty());

        assertTrue(tareaServicio.cambiarEstado(5L, OTRO_USUARIO).isEmpty());
        verify(tareaRepository, never()).save(any(Tarea.class));
    }

    @Test
    void eliminar_deTareaPropia_laElimina() {
        Tarea propia = new Tarea("Propia", "Media");
        when(tareaRepository.findByIdAndUsuarioId(3L, USUARIO)).thenReturn(Optional.of(propia));

        assertTrue(tareaServicio.eliminar(3L, USUARIO));
        verify(tareaRepository).delete(propia);
    }

    @Test
    void eliminar_deTareaAjena_noEliminaNada() {
        when(tareaRepository.findByIdAndUsuarioId(3L, OTRO_USUARIO)).thenReturn(Optional.empty());

        assertFalse(tareaServicio.eliminar(3L, OTRO_USUARIO));
        verify(tareaRepository, never()).delete(any(Tarea.class));
    }
}
