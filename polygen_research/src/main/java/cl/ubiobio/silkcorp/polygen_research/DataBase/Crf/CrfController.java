package cl.ubiobio.silkcorp.polygen_research.DataBase.Crf; 

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfForm;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.export.PdfService;

@Controller
@RequestMapping("/crf")
public class CrfController {

    private final CrfService crfService;
    private final PdfService pdfService;

    public CrfController(CrfService crfService, PdfService pdfService) {
        this.crfService = crfService;
        this.pdfService = pdfService;
    }


    @GetMapping("/nuevo")
    public String mostrarNuevoCrfForm(Model model) {
        CrfForm form = crfService.prepararNuevoCrfForm();

        model.addAttribute("crfForm", form);
        return "dev/CrfTemp/crf-form";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarCrf(@PathVariable("id") Integer crfId, Model model) {

        CrfForm form = crfService.prepararCrfFormParaEditar(crfId);

        model.addAttribute("crfForm", form);


        return "dev/CrfTemp/Crf-form";
    }

    @PostMapping("/guardar")
    public String guardarNuevoCrf(@ModelAttribute CrfForm crfForm, Model model) {

        try {
            if (crfForm.getIdCrf() == null) {

                crfService.guardarCrfCompleto(crfForm);
            } else {
                crfService.actualizarCrfCompleto(crfForm);
            }

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al guardar el CRF: " + e.getMessage());
            model.addAttribute("crfForm", crfForm);
            return "dev/CrfTemp/Crf-form";
        }

        return "redirect:/crf/list";
    }

    @GetMapping("/api/crf/{id}") 
    @ResponseBody
    public ResponseEntity<Crf> getCrfByIdApi(@PathVariable Integer id) {
        return crfService.getCrfById(id) 
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/crf/{id}/missing-data")
    @ResponseBody
    public ResponseEntity<List<String>> getMissingData(@PathVariable("id") Integer crfId) {
        try {
            List<String> missingFields = crfService.getMissingFields(crfId);
            return ResponseEntity.ok(missingFields);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/reporte")
    public String mostrarReporteCrf(Model model) {

        CrfResumenViewDTO data = crfService.getCrfResumenView();

        model.addAttribute("camposColumnas", data.getCamposConStats()); 
        model.addAttribute("filasCrf", data.getFilas());  

        return "crf-reporte";
    }

    @GetMapping("/list")
    public String listarTodosLosCrfs(Model model,
            @RequestParam(name = "codigoPaciente", required = false) String codigoBusqueda) {


        CrfResumenViewDTO data = crfService.getCrfResumenView(codigoBusqueda);

        model.addAttribute("camposColumnas", data.getCamposConStats());
        model.addAttribute("filasCrf", data.getFilas());

        return "dev/CrfTemp/Crf-list"; 
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<InputStreamResource> descargarCrfPdf(@PathVariable("id") Integer crfId) throws IOException {

        Map<String, Object> pdfData = pdfService.generarPdfCrf(crfId);

        ByteArrayInputStream pdfStream = (ByteArrayInputStream) pdfData.get("pdfStream");
        String filename = (String) pdfData.get("filename");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }

}