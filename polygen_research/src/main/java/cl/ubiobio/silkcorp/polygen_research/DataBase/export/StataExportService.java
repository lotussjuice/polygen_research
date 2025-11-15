package cl.ubiobio.silkcorp.polygen_research.DataBase.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CampoCrfStatsDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenRowDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CriterioDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.StataPreviewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.util.CalculoService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.util.StataFormateador;

@Service
public class StataExportService {

    private final CrfService crfService;
    private final CalculoService calculoService;
    private final ExcelReporteService excelReporteService;
    private final ObjectMapper objectMapper;

    public StataExportService(CrfService crfService, CalculoService calculoService, ExcelReporteService excelReporteService) {
        this.crfService = crfService;
        this.calculoService = calculoService;
        this.excelReporteService = excelReporteService;
        this.objectMapper = new ObjectMapper();
    }

    public ByteArrayInputStream generarReporteStata(String criteriosJson) throws IOException {
        StataPreviewDTO data = prepararDatosStata(criteriosJson);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Datos_STATA");

            Row headerRow = sheet.createRow(0);
            List<String> headers = data.getHeadersStata();
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }

            int rowIdx = 1;
            List<Map<String, String>> filas = data.getFilasStata();

            for (Map<String, String> fila : filas) {
                Row dataRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i);
                    String valor = fila.get(header);
                    
                    try {
                        double valorNum = Double.parseDouble(valor);
                        dataRow.createCell(i).setCellValue(valorNum);
                    } catch (NumberFormatException e) {
                        dataRow.createCell(i).setCellValue(valor);
                    }
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public StataPreviewDTO getPreview(String criteriosJson) throws IOException {
        return prepararDatosStata(criteriosJson);
    }

    private StataPreviewDTO prepararDatosStata(String criteriosJson) throws IOException {

        CrfResumenViewDTO data = crfService.getCrfResumenView();
        
        List<CampoCrfStatsDTO> columnasDinamicasStats = data.getCamposConStats();

        
        List<CrfResumenRowDTO> filas = data.getFilas();

        Map<Integer, List<CriterioDTO>> criteriosMap;
        try {
            criteriosMap = objectMapper.readValue(criteriosJson, new TypeReference<HashMap<Integer, List<CriterioDTO>>>() {});
        } catch (IOException e) {
            throw new IOException("Error parseando JSON de criterios", e);
        }

        Map<String, Double> puntosDeCorte = excelReporteService.preCalcularPuntosDeCorte(filas, criteriosMap);

        List<String> headersOriginal = new ArrayList<>();
        List<String> headersStata = new ArrayList<>();
        List<Map<String, String>> filasOriginal = new ArrayList<>();
        List<Map<String, String>> filasStata = new ArrayList<>();

        Map<String, String> fixedHeaders = new LinkedHashMap<>();
        fixedHeaders.put("ID_CRF", "id_crf");
        fixedHeaders.put("Codigo_Paciente", "cod_paciente");
        fixedHeaders.put("Paciente", "paciente");
        fixedHeaders.put("Fecha_Creacion", "fecha_creacion");

        fixedHeaders.forEach((original, stata) -> {
            headersOriginal.add(original);
            headersStata.add(stata);
        });


        for (CampoCrfStatsDTO stat : columnasDinamicasStats) {
            CampoCrf campo = stat.getCampoCrf(); 

            
            headersOriginal.add(campo.getNombre());
            headersStata.add(StataFormateador.formatarNombreVariable(campo.getNombre()));

            if (criteriosMap.containsKey(campo.getIdCampo())) {
                for (CriterioDTO criterio : criteriosMap.get(campo.getIdCampo())) {
                    String nombreColOriginal = excelReporteService.getNombreColumnaDicotomizada(campo.getNombre(), criterio);
                    headersOriginal.add(nombreColOriginal);
                    headersStata.add(StataFormateador.formatarNombreVariable(nombreColOriginal));
                }
            }
        }

        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (CrfResumenRowDTO fila : filas) {
            Map<String, String> filaOrig = new LinkedHashMap<>();
            Map<String, String> filaStata = new LinkedHashMap<>();

            String idCrf = fila.getCrf().getIdCrf().toString();
            String codPac = fila.getCrf().getDatosPaciente() != null ? fila.getCrf().getDatosPaciente().getCodigoPaciente() : "";
            String nomPac = (fila.getCrf().getDatosPaciente() != null) ? fila.getCrf().getDatosPaciente().getNombre() + " " + fila.getCrf().getDatosPaciente().getApellido() : "";
            String fecha = fila.getCrf().getFechaConsulta() != null ? fila.getCrf().getFechaConsulta().format(dateFormatter) : "";

            filaOrig.put("ID_CRF", idCrf);
            filaStata.put("id_crf", StataFormateador.formatarValor(idCrf));
            
            filaOrig.put("Codigo_Paciente", codPac);
            filaStata.put("cod_paciente", StataFormateador.formatarValor(codPac));
            
            filaOrig.put("Paciente", nomPac);
            filaStata.put("paciente", StataFormateador.formatarValor(nomPac));
            
            filaOrig.put("Fecha_Creacion", fecha);
            filaStata.put("fecha_creacion", StataFormateador.formatarValor(fecha));

            for (CampoCrfStatsDTO stat : columnasDinamicasStats) {
                CampoCrf campo = stat.getCampoCrf(); 

            
                Integer campoId = campo.getIdCampo();
                String valorCrudo = fila.getValores().get(campoId);
                valorCrudo = (valorCrudo != null) ? valorCrudo : "";
                
                String headerOrigCrudo = campo.getNombre();
                String headerStataCrudo = StataFormateador.formatarNombreVariable(headerOrigCrudo);
                
                filaOrig.put(headerOrigCrudo, valorCrudo);
                filaStata.put(headerStataCrudo, StataFormateador.formatarValor(valorCrudo));

                if (criteriosMap.containsKey(campoId)) {
                    double valorNum = excelReporteService.parseDouble(valorCrudo);
                    
                    for (CriterioDTO criterio : criteriosMap.get(campoId)) {
                        
                        String valorDicoStr;
                        
                        if (Double.isNaN(valorNum)) {
                            valorDicoStr = "";
                        } else {
                            double puntoCorteCalculado = puntosDeCorte.getOrDefault(campoId + "_" + criterio.getTipo(), 0.0);
                    
                            int valorDicotomizado = excelReporteService.calcularValorDicotomizado(
                                valorNum,
                                criterio, 
                                puntoCorteCalculado
                            );
                            valorDicoStr = String.valueOf(valorDicotomizado);
                        }

                        String headerOrigDico = excelReporteService.getNombreColumnaDicotomizada(campo.getNombre(), criterio);
                        String headerStataDico = StataFormateador.formatarNombreVariable(headerOrigDico);
                        
                        filaOrig.put(headerOrigDico, valorDicoStr);
                        filaStata.put(headerStataDico, valorDicoStr);
                    }
                }
            }
            filasOriginal.add(filaOrig);
            filasStata.add(filaStata);
        }

        return new StataPreviewDTO(headersOriginal, headersStata, filasOriginal, filasStata);
    }
}