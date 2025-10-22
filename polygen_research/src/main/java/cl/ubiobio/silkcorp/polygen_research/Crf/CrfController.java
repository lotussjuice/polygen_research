package cl.ubiobio.silkcorp.polygen_research.Crf;

import cl.ubiobio.silkcorp.polygen_research.DatosPaciente.DatosPacienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/crfs")
public class CrfController {

    private final CrfService crfService;
    private final DatosPacienteService pacienteService;

    public CrfController(CrfService crfService, DatosPacienteService pacienteService) {
        this.crfService = crfService;
        this.pacienteService = pacienteService;
    }

    @GetMapping("/list")
    public String listarCrfs(Model model) {
        List<Crf> listaCrfs = crfService.getAllCrfs();
        model.addAttribute("crfs", listaCrfs);
        return "crf-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDeNuevoCrf(Model model) {
        model.addAttribute("crf", new Crf());
        model.addAttribute("pacientes", pacienteService.getAllPacientes());
        return "crf-form";
    }

    @PostMapping("/guardar")
    public String guardarCrf(@ModelAttribute Crf crf) {
        crfService.saveCrf(crf);
        return "redirect:/crfs/list";
    }
}