package cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.RequestMapping;

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
        return "dev/CampoCrfTemp/campo-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDeNuevoCampo(Model model) {
        model.addAttribute("campo", new CampoCrf());
        return "dev/CampoCrfTemp/campo-form";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEditar(@PathVariable Integer id, Model model) {
        CampoCrf campo = campoCrfService.getCampoById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de campo inválido:" + id));
        model.addAttribute("campo", campo);
        return "dev/CampoCrfTemp/campo-form";
    }

    @PostMapping("/guardar")
    public String guardarCampo(@ModelAttribute CampoCrf campo) {
        campoCrfService.saveCampo(campo);
        return "redirect:/campos/list";
    }

    @GetMapping("/desactivar/{id}")
    public String toggleCampoEstado(@PathVariable Integer id) {
        campoCrfService.toggleEstadoCampo(id);
        return "redirect:/campos/list";
    }

}