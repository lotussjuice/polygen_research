package cl.ubiobio.silkcorp.polygen_research.Whitelist;

import cl.ubiobio.silkcorp.polygen_research.Usuario.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Controller
@RequestMapping("/whitelist")
public class WhitelistController {

    private final WhitelistService whitelistService;
    private final UsuarioService usuarioService;

    public WhitelistController(WhitelistService whitelistService, UsuarioService usuarioService) {
        this.whitelistService = whitelistService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/list")
    public String listarCredenciales(Model model) {
        List<Whitelist> listaCredenciales = whitelistService.getAllCredenciales();
        model.addAttribute("credenciales", listaCredenciales);
        return "WhitelistTemp/whitelist-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("credencial", new Whitelist());
        model.addAttribute("usuarios", usuarioService.getAllUsuarios());
        return "WhitelistTemp/whitelist-form";
    }

    @PostMapping("/guardar")
    public String guardarCredencial(@ModelAttribute Whitelist credencial) {
        whitelistService.saveCredencial(credencial);
        return "redirect:/whitelist/list";
    }
}