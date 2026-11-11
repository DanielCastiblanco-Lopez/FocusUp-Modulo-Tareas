package com.focusup.repositorio;

import com.focusup.modelo.Rol;
import com.focusup.modelo.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repositorio JPA para la entidad {@link Usuario}.
 *
 * Spring Data JPA genera la implementación en tiempo de ejecución a
 * partir de los nombres de los métodos.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCedula(String cedula);

    boolean existsByCedulaAndIdNot(String cedula, Long id);

    List<Usuario> findAllByOrderByFechaRegistroDesc();

    /** Cuántos usuarios hay con ese rol (se usa para proteger al último administrador). */
    long countByRol(Rol rol);

    boolean existsByRol(Rol rol);

    /**
     * Completa las columnas nuevas en cuentas anteriores a la integración
     * (se ejecuta una vez al arrancar, ver InicializadorDatos).
     */
    @Modifying
    @Transactional
    @Query("update Usuario u set u.rol = com.focusup.modelo.Rol.USUARIO where u.rol is null")
    int completarRolesVacios();

    @Modifying
    @Transactional
    @Query("update Usuario u set u.activo = true where u.activo is null")
    int completarActivosVacios();
}
