package com.focusup.repositorio;

import com.focusup.modelo.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad {@link Usuario}.
 *
 * Spring Data JPA genera automáticamente la implementación de esta
 * interfaz en tiempo de ejecución (no hay que escribir el SQL a mano).
 *
 * @author Daniel Castiblanco
 * @version 1.0
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo. Se usa tanto en el registro
     * (para validar que el correo no exista ya) como en el login
     * (para encontrar la cuenta y comparar la contraseña).
     *
     * Spring Data JPA genera la consulta automáticamente a partir del
     * nombre del método: "findByEmail" → "SELECT * FROM usuarios WHERE email = ?".
     */
    Optional<Usuario> findByEmail(String email);

    /** true si ya existe una cuenta registrada con ese correo. */
    boolean existsByEmail(String email);
}
