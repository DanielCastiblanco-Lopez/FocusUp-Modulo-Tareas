package com.focusup.controlador;

import com.focusup.dto.CambioContrasenaForm;
import com.focusup.dto.PerfilForm;
import com.focusup.excepcion.ReglaNegocioException;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * "Mi cuenta": cualquier usuario autenticado ve y edita SOLO su propia información.
 *
 * Por diseño las rutas no llevan id: siempre se trabaja con el usuario de
 * la sesión. Por eso no existe forma de pedir ni modificar los datos de
 * otra persona desde aquí, y el formulario tampoco recibe rol ni estado.
 *
 * Rutas:
 * GET  /cuenta              ver mis datos
 * POST /cuenta              guardar mis datos
 * POST /cuenta/contrasena   cambiar mi contraseña (pide la actual)
 *
 * @author Daniel Castiblanco
 * @version 2.0
 */
@Controller
@RequestMapping("/cuenta")
public class CuentaControlador {

    private final UsuarioServicio usuarioServicio;

    public CuentaControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    public String ver(HttpServletRequest request, Model model) {
        Usuario usuario = Sesion.usuarioActual(request);
        model.addAttribute("perfil", perfilDe(usuario));
        model.addAttribute("cambio", new CambioContrasenaForm());
        return "cuenta/perfil";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("perfil") PerfilForm perfil,
                          BindingResult resultado,
                          HttpServletRequest request, Model model,
                          RedirectAttributes redirect) {
        Usuario usuario = Sesion.usuarioActual(request);
        if (!resultado.hasErrors()) {
            try {
                usuarioServicio.actualizarPerfil(usuario.getId(), perfil);
                redirect.addFlashAttribute("exito", "Tus datos se actualizaron correctamente.");
                return "redirect:/cuenta";
            } catch (ReglaNegocioException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("cambio", new CambioContrasenaForm());
        return "cuenta/perfil";
    }

    @PostMapping("/contrasena")
    public String cambiarContrasena(@Valid @ModelAttribute("cambio") CambioContrasenaForm cambio,
                                    BindingResult resultado,
                                    HttpServletRequest request, Model model,
                                    RedirectAttributes redirect) {
        Usuario usuario = Sesion.usuarioActual(request);
        if (!resultado.hasErrors()) {
            try {
                usuarioServicio.cambiarContrasenaPropia(usuario.getId(),
                        cambio.getActual(), cambio.getNueva(), cambio.getConfirmar());
                redirect.addFlashAttribute("exito", "Tu contraseña se cambió correctamente.");
                return "redirect:/cuenta";
            } catch (ReglaNegocioException e) {
                model.addAttribute("errorContrasena", e.getMessage());
            }
        }
        model.addAttribute("perfil", perfilDe(usuario));
        return "cuenta/perfil";
    }

    static PerfilForm perfilDe(Usuario usuario) {
        PerfilForm form = new PerfilForm();
        form.setNombres(usuario.getNombres());
        form.setApellidos(usuario.getApellidos());
        form.setCedula(usuario.getCedula());
        form.setEmail(usuario.getEmail());
        form.setFechaNacimiento(usuario.getFechaNacimiento());
        return form;
    }
}
