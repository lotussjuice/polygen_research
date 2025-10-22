package cl.ubiobio.silkcorp.polygen_research.Usuario;

import cl.ubiobio.silkcorp.polygen_research.RolUsuario.RolUsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolUsuarioService rolUsuarioService;

    public UsuarioController(UsuarioService usuarioService, RolUsuarioService rolUsuarioService) {
        this.usuarioService = usuarioService;
        this.rolUsuarioService = rolUsuarioService;
    }

    @GetMapping("/list")
    public String listarUsuarios(Model model) {
        List<Usuario> listaUsuarios = usuarioService.getAllUsuarios();
        model.addAttribute("usuarios", listaUsuarios);
        return "usuario-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDeNuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", rolUsuarioService.getAllRoles());
        return "usuario-form";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {
        usuarioService.saveUsuario(usuario);
        return "redirect:/usuarios/list";
    }
}