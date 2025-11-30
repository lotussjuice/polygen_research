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
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.StataPreviewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.export.ExcelReporteService; 
import cl.ubiobio.silkcorp.polygen_research.DataBase.export.StataExportService;

@Controller
@RequestMapping("/datos-crf")
public class DatosCrfController {

    private final CrfService crfService;
    private final ExcelReporteService excelReporteService;
    private final StataExportService stataExportService;
    
    public DatosCrfController(DatosCrfService datosCrfService, CrfService crfService, CampoCrfService campoCrfService, ExcelReporteService excelReporteService, StataExportService stataExportService) {
        this.crfService = crfService;
        this.excelReporteService = excelReporteService;
        this.stataExportService = stataExportService;
    }

    @GetMapping("/list")
    public String mostrarReporteDeDatos(Model model,
                                        @RequestParam(name = "codigoPaciente", required = false) String codigoBusqueda) {

        CrfResumenViewDTO data = crfService.getCrfResumenView(codigoBusqueda, true);

        model.addAttribute("camposColumnas", data.getCamposConStats());
        model.addAttribute("filasCrf", data.getFilas());

        return "dev/DatosCrfTemp/datos-crf-list"; 
    }

   @PostMapping("/list/exportar")
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
    @ResponseBody
    public ResponseEntity<StataPreviewDTO> getStataPreview(@RequestParam("criteriosJson") String criteriosJson) {
        try {
            StataPreviewDTO previewData = stataExportService.getPreview(criteriosJson);
            return ResponseEntity.ok(previewData);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
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