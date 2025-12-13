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

    private final DatosCrfService datosCrfService;
    private final CrfService crfService;
    private final CampoCrfService campoCrfService;
    private final ExcelReporteService excelReporteService;
    private final StataExportService stataExportService;

    public DatosCrfController(DatosCrfService datosCrfService, CrfService crfService, CampoCrfService campoCrfService, ExcelReporteService excelReporteService, StataExportService stataExportService) {
        this.datosCrfService = datosCrfService;
        this.crfService = crfService;
        this.campoCrfService = campoCrfService;
        this.excelReporteService = excelReporteService;
        this.stataExportService = stataExportService;
    }

    @GetMapping("/list")
    public String listarDatosCrf(Model model, @RequestParam(required = false) String codigoPaciente) {
        String codigoBusqueda = (codigoPaciente != null && !codigoPaciente.isEmpty()) ? codigoPaciente : null;
        CrfResumenViewDTO data = crfService.getCrfResumenView(codigoBusqueda, true);
        model.addAttribute("camposColumnas", data.getCamposConStats());
        model.addAttribute("filasCrf", data.getFilas());
        return "dev/DatosCrfTemp/datos-crf-list";
    }

    @PostMapping("/list/exportar")
    public ResponseEntity<InputStreamResource> exportarReporte(
            @RequestParam("criteriosJson") String criteriosJson,
            @RequestParam("excludedColsJson") String excludedColsJson) throws IOException {

        ByteArrayInputStream stream = excelReporteService.generarReporteDicotomizado(criteriosJson, excludedColsJson);

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String nombreArchivo = "Reporte_Polygen_" + fecha + ".xlsx";

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
            // Nota: El preview podría no filtrar columnas, pero la exportación final sí lo hará.
            StataPreviewDTO previewData = stataExportService.getPreview(criteriosJson);
            return ResponseEntity.ok(previewData);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // MÉTODO ACTUALIZADO PARA RECIBIR excludedColsJson
    @PostMapping("/list/exportar-stata")
    public ResponseEntity<InputStreamResource> exportarReporteStata(
            @RequestParam("criteriosJson") String criteriosJson,
            @RequestParam("excludedColsJson") String excludedColsJson) throws IOException {

        ByteArrayInputStream stream = stataExportService.generarReporteStata(criteriosJson, excludedColsJson);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_STATA.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }
}