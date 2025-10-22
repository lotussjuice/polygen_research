package cl.ubiobio.silkcorp.polygen_research.DatosCrf;

import cl.ubiobio.silkcorp.polygen_research.CampoCrf.CampoCrfService;
import cl.ubiobio.silkcorp.polygen_research.Crf.CrfService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/datos-crf")
public class DatosCrfController {

    private final DatosCrfService datosCrfService;
    private final CrfService crfService;
    private final CampoCrfService campoCrfService;

    public DatosCrfController(DatosCrfService datosCrfService, CrfService crfService, CampoCrfService campoCrfService) {
        this.datosCrfService = datosCrfService;
        this.crfService = crfService;
        this.campoCrfService = campoCrfService;
    }

    @GetMapping("/list")
    public String listarDatos(Model model) {
        List<DatosCrf> listaDatos = datosCrfService.getAllDatosCrf();
        model.addAttribute("datos", listaDatos);
        return "DatosCrfTemp/datos-crf-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDeNuevoDato(Model model) {
        model.addAttribute("dato", new DatosCrf());
        model.addAttribute("crfs", crfService.getAllCrfs());
        model.addAttribute("campos", campoCrfService.getAllCampos());
        return "DatosCrfTemp/datos-crf-form";
    }

    @PostMapping("/guardar")
    public String guardarDato(@ModelAttribute DatosCrf dato) {
        datosCrfService.saveDatoCrf(dato);
        return "redirect:/datos-crf/list";
    }
}