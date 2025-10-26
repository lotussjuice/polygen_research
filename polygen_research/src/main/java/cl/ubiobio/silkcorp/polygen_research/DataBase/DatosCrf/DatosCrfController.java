package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.export.ExcelReporteService;

@Controller
@RequestMapping("/datos-crf")
public class DatosCrfController {

    //private final DatosCrfService datosCrfService;
    private final CrfService crfService;
    //private final CampoCrfService campoCrfService;
    private final ExcelReporteService excelReporteService;

    public DatosCrfController(DatosCrfService datosCrfService, CrfService crfService, CampoCrfService campoCrfService, ExcelReporteService excelReporteService) {
        //this.datosCrfService = datosCrfService;
        this.crfService = crfService;
        //this.campoCrfService = campoCrfService;
        this.excelReporteService = excelReporteService;
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


    /*
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

    */

   @PostMapping("/list/exportar") // Esta URL coincide con el 'th:action' del form
    public ResponseEntity<InputStreamResource> exportarReporte(
            @RequestParam("criteriosJson") String criteriosJson) throws IOException {

        ByteArrayInputStream stream = excelReporteService.generarReporteDicotomizado(criteriosJson);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ReporteDicotomizado.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }
}