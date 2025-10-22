package cl.ubiobio.silkcorp.polygen_research.RolUsuario;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/roles")
public class RolUsuarioController {

    private final RolUsuarioService rolUsuarioService;

    public RolUsuarioController(RolUsuarioService rolUsuarioService) {
        this.rolUsuarioService = rolUsuarioService;
    }

    @GetMapping("/list")
    public String listarRoles(Model model) {
        List<RolUsuario> listaRoles = rolUsuarioService.getAllRoles();
        model.addAttribute("roles", listaRoles);
        return "RolUsuarioTemp/rol-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDeNuevoRol(Model model) {
        model.addAttribute("rol", new RolUsuario());
        return "RolUsuarioTemp/rol-form";
    }

    @PostMapping("/guardar")
    public String guardarRol(@ModelAttribute RolUsuario rol) {
        rolUsuarioService.saveRol(rol);
        return "redirect:/roles/list";
    }
}