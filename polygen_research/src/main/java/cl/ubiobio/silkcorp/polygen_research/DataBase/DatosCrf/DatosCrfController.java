package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
import org.springframework.web.bind.annotation.ResponseBody;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.StataPreviewDTO; // NUEVO IMPORT
import cl.ubiobio.silkcorp.polygen_research.DataBase.export.ExcelReporteService; // NUEVO IMPORT
import cl.ubiobio.silkcorp.polygen_research.DataBase.export.StataExportService; // NUEVO IMPORT

@Controller
@RequestMapping("/datos-crf")
public class DatosCrfController {

    //private final DatosCrfService datosCrfService;
    private final CrfService crfService;
    //private final CampoCrfService campoCrfService;
    private final ExcelReporteService excelReporteService;
    private final StataExportService stataExportService;
    
    public DatosCrfController(DatosCrfService datosCrfService, CrfService crfService, CampoCrfService campoCrfService, ExcelReporteService excelReporteService, StataExportService stataExportService) {
        //this.datosCrfService = datosCrfService;
        this.crfService = crfService;
        //this.campoCrfService = campoCrfService;
        this.excelReporteService = excelReporteService;
        this.stataExportService = stataExportService;
    }

    @GetMapping("/list")
    public String mostrarReporteDeDatos(Model model,
            @RequestParam(name = "codigoPaciente", required = false) String codigoBusqueda) {
        CrfResumenViewDTO data = crfService.getCrfResumenView(codigoBusqueda);

        model.addAttribute("camposColumnas", data.getCamposActivos());
        model.addAttribute("filasCrf", data.getFilas());

        // Return the logical name of the view template
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
        String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String nombreArchivo = "ReporteDicotomizado_" + fechaActual + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

    @PostMapping("/api/preview-stata")
    @ResponseBody // Devuelve JSON
    public ResponseEntity<StataPreviewDTO> getStataPreview(@RequestParam("criteriosJson") String criteriosJson) {
        try {
            StataPreviewDTO previewData = stataExportService.getPreview(criteriosJson);
            return ResponseEntity.ok(previewData);
        } catch (IOException e) {
            // Manejo de error (simplificado)
            return ResponseEntity.badRequest().build();
        }
    }
    
    // --- NUEVO ENDPOINT PARA DESCARGA STATA ---
    @PostMapping("/list/exportar-stata")
    public ResponseEntity<InputStreamResource> exportarReporteStata(
            @RequestParam("criteriosJson") String criteriosJson) throws IOException {

        ByteArrayInputStream stream = stataExportService.generarReporteStata(criteriosJson);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_STATA.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

}