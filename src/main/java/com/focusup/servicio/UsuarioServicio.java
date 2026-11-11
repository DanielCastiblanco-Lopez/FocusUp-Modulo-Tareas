package com.focusup.servicio;

import com.focusup.dto.AdminUsuarioForm;
import com.focusup.dto.PerfilForm;
import com.focusup.excepcion.ReglaNegocioException;
import com.focusup.modelo.Rol;
import com.focusup.modelo.Usuario;
import com.focusup.repositorio.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de usuarios (módulo web de usuarios integrado).
 *
 * Aquí viven las reglas de negocio que protegen el sistema. NO decide
 * quién puede llamar a cada método (eso lo hace la capa de seguridad:
 * interceptores y controladores), pero sí impide que, aun siendo
 * administrador, alguien deje el sistema sin administradores.
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@Service
@Transactional
public class UsuarioServicio {

    private static final int LONGITUD_MIN_CONTRASENA = 6;

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioServicio(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByFechaRegistroDesc();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado"));
    }

    /**
     * Actualiza los datos personales (nombres, apellidos, cédula, correo y
     * fecha de nacimiento). No toca rol ni estado.
     */
    public Usuario actualizarPerfil(Long id, PerfilForm form) {
        Usuario usuario = obtener(id);
        aplicarPerfil(usuario, form);
        return usuarioRepository.save(usuario);
    }

    /**
     * Edición completa hecha por un administrador: datos personales, rol y estado.
     *
     * @param adminId id del administrador que hace el cambio
     * @param id      id del usuario que se edita
     */
    public Usuario actualizarComoAdmin(Long adminId, Long id, AdminUsuarioForm form) {
        Usuario usuario = obtener(id);
        validarCambioDeRolOEstado(adminId, usuario, form.getRol(), form.isActivo());
        aplicarPerfil(usuario, form);
        usuario.setRol(form.getRol());
        usuario.setActivo(form.isActivo());
        return usuarioRepository.save(usuario);
    }

    /** Activa o desactiva (baja lógica) a un usuario. Solo para administradores. */
    public Usuario cambiarEstado(Long adminId, Long id, boolean activo) {
        Usuario usuario = obtener(id);
        validarCambioDeRolOEstado(adminId, usuario, usuario.getRol(), activo);
        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }

    /** El propio usuario cambia su contraseña: debe conocer la actual. */
    public void cambiarContrasenaPropia(Long id, String actual, String nueva, String confirmar) {
        Usuario usuario = obtener(id);
        if (actual == null || !passwordEncoder.matches(actual, usuario.getPassword())) {
            throw new ReglaNegocioException("La contraseña actual no es correcta");
        }
        guardarNuevaContrasena(usuario, nueva, confirmar);
    }

    /** Un administrador restablece la contraseña de otro usuario (sin pedir la actual). */
    public void restablecerContrasena(Long id, String nueva, String confirmar) {
        guardarNuevaContrasena(obtener(id), nueva, confirmar);
    }

    // ── Reglas internas ─────────────────────────────────────────────────────

    private void aplicarPerfil(Usuario usuario, PerfilForm form) {
        String email = AuthServicio.normalizarCorreo(form.getEmail());
        String cedula = form.getCedula().trim();

        if (usuarioRepository.existsByEmailAndIdNot(email, usuario.getId())) {
            throw new ReglaNegocioException("Ese correo ya pertenece a otro usuario");
        }
        if (usuarioRepository.existsByCedulaAndIdNot(cedula, usuario.getId())) {
            throw new ReglaNegocioException("Esa cédula ya pertenece a otro usuario");
        }

        usuario.setNombres(form.getNombres().trim());
        usuario.setApellidos(form.getApellidos().trim());
        usuario.setCedula(cedula);
        usuario.setEmail(email);
        usuario.setFechaNacimiento(form.getFechaNacimiento());
    }

    /**
     * Dos reglas para no dejar el sistema sin administración:
     * 1) un administrador no puede quitarse su propio rol ni desactivarse;
     * 2) nunca se puede dejar el sistema sin al menos un administrador activo.
     */
    private void validarCambioDeRolOEstado(Long adminId, Usuario objetivo, Rol nuevoRol, boolean nuevoActivo) {
        boolean pierdeAdmin = objetivo.esAdmin() && objetivo.isActivo() && (nuevoRol != Rol.ADMIN || !nuevoActivo);
        if (!pierdeAdmin) {
            return;
        }
        if (objetivo.getId().equals(adminId)) {
            throw new ReglaNegocioException("No puedes quitarte tu propio rol de administrador ni desactivarte");
        }
        if (contarAdminsActivos() <= 1) {
            throw new ReglaNegocioException("Debe quedar al menos un administrador activo");
        }
    }

    private long contarAdminsActivos() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.esAdmin() && u.isActivo())
                .count();
    }

    private void guardarNuevaContrasena(Usuario usuario, String nueva, String confirmar) {
        if (nueva == null || nueva.length() < LONGITUD_MIN_CONTRASENA) {
            throw new ReglaNegocioException("La nueva contraseña debe tener al menos " + LONGITUD_MIN_CONTRASENA + " caracteres");
        }
        if (!nueva.equals(confirmar)) {
            throw new ReglaNegocioException("Las contraseñas no coinciden");
        }
        usuario.setPassword(passwordEncoder.encode(nueva));
        usuarioRepository.save(usuario);
    }
}
