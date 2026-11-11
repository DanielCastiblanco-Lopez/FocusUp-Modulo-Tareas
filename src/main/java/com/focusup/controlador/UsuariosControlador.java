package com.focusup.controlador;

import com.focusup.dto.AdminUsuarioForm;
import com.focusup.dto.CambioContrasenaForm;
import com.focusup.excepcion.ReglaNegocioException;
import com.focusup.modelo.Rol;
import com.focusup.modelo.Usuario;
import com.focusup.seguridad.Sesion;
import com.focusup.servicio.UsuarioServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Administración de usuarios: solo para ADMIN (el acceso lo exige
 * {@code AdminInterceptor} sobre /usuarios/**). Es el antiguo UsuarioServlet
 * del módulo web, ahora dentro de la aplicación unificada.
 *
 * Rutas:
 * GET  /usuarios                      lista de todos los usuarios
 * GET  /usuarios/{id}/editar          formulario de edición
 * POST /usuarios/{id}                 guarda datos, rol y estado
 * POST /usuarios/{id}/contrasena      restablece la contraseña
 * POST /usuarios/{id}/estado          activa o desactiva (baja lógica)
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@Controller
@RequestMapping("/usuarios")
public class UsuariosControlador {

    private final UsuarioServicio usuarioServicio;

    public UsuariosControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioServicio.listar());
        return "usuarios/listar";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            cargarFormulario(model, usuarioServicio.obtener(id));
            model.addAttribute("cambio", new CambioContrasenaForm());
            return "usuarios/formulario";
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    @PostMapping("/{id}")
    public String guardar(@PathVariable Long id,
                          @Valid @ModelAttribute("form") AdminUsuarioForm form,
                          BindingResult resultado,
                          HttpServletRequest request, Model model,
                          RedirectAttributes redirect) {
        Usuario admin = Sesion.usuarioActual(request);
        try {
            if (!resultado.hasErrors()) {
                usuarioServicio.actualizarComoAdmin(admin.getId(), id, form);
                redirect.addFlashAttribute("exito", "Usuario actualizado correctamente.");
                return "redirect:/usuarios";
            }
            model.addAttribute("objetivo", usuarioServicio.obtener(id));
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            try {
                model.addAttribute("objetivo", usuarioServicio.obtener(id));
            } catch (ReglaNegocioException noExiste) {
                redirect.addFlashAttribute("error", noExiste.getMessage());
                return "redirect:/usuarios";
            }
        }
        model.addAttribute("roles", Rol.values());
        model.addAttribute("cambio", new CambioContrasenaForm());
        return "usuarios/formulario";
    }

    @PostMapping("/{id}/contrasena")
    public String restablecerContrasena(@PathVariable Long id,
                                        @Valid @ModelAttribute("cambio") CambioContrasenaForm cambio,
                                        BindingResult resultado,
                                        Model model, RedirectAttributes redirect) {
        try {
            Usuario objetivo = usuarioServicio.obtener(id);
            if (!resultado.hasErrors()) {
                try {
                    usuarioServicio.restablecerContrasena(id, cambio.getNueva(), cambio.getConfirmar());
                    redirect.addFlashAttribute("exito", "Contraseña restablecida correctamente.");
                    return "redirect:/usuarios/" + id + "/editar";
                } catch (ReglaNegocioException e) {
                    model.addAttribute("errorContrasena", e.getMessage());
                }
            }
            cargarFormulario(model, objetivo);
            return "usuarios/formulario";
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
                                HttpServletRequest request, RedirectAttributes redirect) {
        Usuario admin = Sesion.usuarioActual(request);
        try {
            usuarioServicio.cambiarEstado(admin.getId(), id, activo);
            redirect.addFlashAttribute("exito", activo ? "Usuario activado." : "Usuario desactivado.");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    private void cargarFormulario(Model model, Usuario usuario) {
        AdminUsuarioForm form = new AdminUsuarioForm();
        form.setNombres(usuario.getNombres());
        form.setApellidos(usuario.getApellidos());
        form.setCedula(usuario.getCedula());
        form.setEmail(usuario.getEmail());
        form.setFechaNacimiento(usuario.getFechaNacimiento());
        form.setRol(usuario.getRol());
        form.setActivo(usuario.isActivo());
        model.addAttribute("form", form);
        model.addAttribute("objetivo", usuario);
        model.addAttribute("roles", Rol.values());
        if (!model.containsAttribute("cambio")) {
            model.addAttribute("cambio", new CambioContrasenaForm());
        }
    }
}
