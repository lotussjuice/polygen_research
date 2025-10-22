package cl.ubiobio.silkcorp.polygen_research.Whitelist;

import cl.ubiobio.silkcorp.polygen_research.Usuario.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/whitelist")
public class WhitelistController {

    private final WhitelistService whitelistService;
    private final UsuarioService usuarioService;

    public WhitelistController(WhitelistService whitelistService, UsuarioService usuarioService) {
        this.whitelistService = whitelistService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registrar")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("credencial", new Whitelist());
        model.addAttribute("usuarios", usuarioService.getAllUsuarios());
        return "whitelist-form";
    }

    @PostMapping("/guardar")
    public String guardarCredencial(@ModelAttribute Whitelist credencial) {
        whitelistService.saveCredencial(credencial);
        return "redirect:/usuarios/list";
    }
}