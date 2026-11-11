package com.focusup.config;

import com.focusup.modelo.Rol;
import com.focusup.modelo.Tarea;
import com.focusup.modelo.Usuario;
import com.focusup.repositorio.TareaRepository;
import com.focusup.repositorio.UsuarioRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prepara los datos al arrancar. Es idempotente: se puede ejecutar muchas veces.
 *
 * 1) Completa rol y estado en cuentas anteriores a la integración.
 * 2) Si no existe ningún administrador, crea el administrador inicial
 *    (o asciende a la cuenta con ese correo si ya existe).
 * 3) Asigna al administrador las tareas que no tenían dueño.
 *
 * Las credenciales iniciales salen de application.properties
 * ({@code focusup.admin.email} y {@code focusup.admin.password}). Debe
 * cambiarse la contraseña desde "Mi cuenta" en el primer ingreso.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@Component
public class InicializadorDatos implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorDatos.class);

    private final UsuarioRepository usuarioRepository;
    private final TareaRepository tareaRepository;
    private final String adminEmail;
    private final String adminPassword;

    public InicializadorDatos(UsuarioRepository usuarioRepository,
                              TareaRepository tareaRepository,
                              @Value("${focusup.admin.email:admin@focusup.com}") String adminEmail,
                              @Value("${focusup.admin.password:Admin123*}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.tareaRepository = tareaRepository;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        usuarioRepository.completarRolesVacios();
        usuarioRepository.completarActivosVacios();

        if (!usuarioRepository.existsByRol(Rol.ADMIN)) {
            Usuario admin = usuarioRepository.findByEmail(adminEmail).orElseGet(() -> {
                Usuario nuevo = new Usuario(adminEmail, new BCryptPasswordEncoder().encode(adminPassword));
                nuevo.setNombres("Administrador");
                nuevo.setApellidos("FocusUp");
                return nuevo;
            });
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true);
            usuarioRepository.save(admin);
            log.info("Administrador inicial disponible: {}", adminEmail);
        }

        List<Tarea> huerfanas = tareaRepository.findByUsuarioIdIsNull();
        if (!huerfanas.isEmpty()) {
            Long adminId = usuarioRepository.findByEmail(adminEmail)
                    .map(Usuario::getId)
                    .orElseGet(() -> usuarioRepository.findAll().stream()
                            .filter(Usuario::esAdmin).findFirst().map(Usuario::getId).orElse(null));
            if (adminId != null) {
                huerfanas.forEach(t -> t.setUsuarioId(adminId));
                tareaRepository.saveAll(huerfanas);
                log.info("{} tareas sin dueño asignadas al administrador", huerfanas.size());
            }
        }
    }
}
