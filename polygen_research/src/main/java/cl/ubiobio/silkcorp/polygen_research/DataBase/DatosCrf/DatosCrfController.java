package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO;
//import java.util.List;

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
    public String mostrarReporteDeDatos(Model model) {
        
        // 1. Llama al método del servicio que pivota los datos
        CrfResumenViewDTO data = crfService.getCrfResumenView();
        
        // 2. Pasamos las dos variables que el HTML necesita
        model.addAttribute("camposColumnas", data.getCamposActivos()); // Los <th>
        model.addAttribute("filasCrf", data.getFilas());         // Los <tr>
        
        // 3. Devuelve el nombre del archivo HTML que te pasaste
        return "dev/DatosCrfTemp/datos-crf-list"; 
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDeNuevoDato(Model model) {
        model.addAttribute("dato", new DatosCrf());
        model.addAttribute("crfs", crfService.getAllCrfs());
        model.addAttribute("campos", campoCrfService.getAllCampos());
        return "Dev/DatosCrfTemp/datos-crf-form";
    }

    @PostMapping("/guardar")
    public String guardarDato(@ModelAttribute DatosCrf dato) {
        datosCrfService.saveDatoCrf(dato);
        return "redirect:/datos-crf/list";
    }
}