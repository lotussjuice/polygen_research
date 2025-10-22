package cl.ubiobio.silkcorp.polygen_research.CampoCrf;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/campos")
public class CampoCrfController {

    private final CampoCrfService campoCrfService;

    public CampoCrfController(CampoCrfService campoCrfService) {
        this.campoCrfService = campoCrfService;
    }

    @GetMapping("/list")
    public String listarCampos(Model model) {
        List<CampoCrf> listaCampos = campoCrfService.getAllCampos();
        model.addAttribute("campos", listaCampos);
        return "CampoCrfTemp/campo-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDeNuevoCampo(Model model) {
        model.addAttribute("campo", new CampoCrf());
        return "CampoCrfTemp/campo-form";
    }

    @PostMapping("/guardar")
    public String guardarCampo(@ModelAttribute CampoCrf campo) {
        campoCrfService.saveCampo(campo);
        return "redirect:/campos/list";
    }
}