package cl.ubiobio.silkcorp.polygen_research.DataBase.export;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenRowDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CriterioDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.StataPreviewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.util.CalculoService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.util.StataFormateador;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap; // Importante para mantener el orden
import java.util.List;
import java.util.Map;

@Service
public class StataExportService {

    private final CrfService crfService;
    private final CalculoService calculoService;
    private final ExcelReporteService excelReporteService; // Reutilizaremos lógica
    private final ObjectMapper objectMapper;

    public StataExportService(CrfService crfService, CalculoService calculoService, ExcelReporteService excelReporteService) {
        this.crfService = crfService;
        this.calculoService = calculoService;
        this.excelReporteService = excelReporteService; // Inyectar el servicio existente
        this.objectMapper = new ObjectMapper();
    }


    public ByteArrayInputStream generarReporteStata(String criteriosJson) throws IOException {
        // 1. Obtener los datos preparados (DTO)
        StataPreviewDTO data = prepararDatosStata(criteriosJson);

        // 2. Usar Apache POI para escribir el archivo
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Datos_STATA");

            // 3. Escribir Encabezado (Headers)
            Row headerRow = sheet.createRow(0);
            List<String> headers = data.getHeadersStata();
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }

            // 4. Escribir Filas de Datos
            int rowIdx = 1;
            List<Map<String, String>> filas = data.getFilasStata();

            for (Map<String, String> fila : filas) {
                Row dataRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i);
                    String valor = fila.get(header); // Obtener valor por header STATA
                    
                    // STATA trata los números como números
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

        // --- 1. OBTENER DATOS (Lógica similar a ExcelReporteService) ---
        CrfResumenViewDTO data = crfService.getCrfResumenView();
        List<CampoCrf> columnasDinamicas = data.getCamposActivos();
        List<CrfResumenRowDTO> filas = data.getFilas();

        // Parsear criterios
        Map<Integer, List<CriterioDTO>> criteriosMap;
        try {
            criteriosMap = objectMapper.readValue(criteriosJson, new TypeReference<HashMap<Integer, List<CriterioDTO>>>() {});
        } catch (IOException e) {
            throw new IOException("Error parseando JSON de criterios", e);
        }

        // Pre-calcular medias y medianas
        Map<String, Double> puntosDeCorte = excelReporteService.preCalcularPuntosDeCorte(filas, criteriosMap);

        // --- 2. PREPARAR ESTRUCTURAS DE DATOS ---
        List<String> headersOriginal = new ArrayList<>();
        List<String> headersStata = new ArrayList<>();
        List<Map<String, String>> filasOriginal = new ArrayList<>();
        List<Map<String, String>> filasStata = new ArrayList<>();

        // --- 3. PROCESAR HEADERS (ENCABEZADOS) ---
        
        // Headers Fijos
        Map<String, String> fixedHeaders = new LinkedHashMap<>();
        fixedHeaders.put("ID_CRF", "id_crf");
        fixedHeaders.put("Codigo_Paciente", "cod_paciente");
        fixedHeaders.put("Paciente", "paciente");
        fixedHeaders.put("Fecha_Creacion", "fecha_creacion");

        fixedHeaders.forEach((original, stata) -> {
            headersOriginal.add(original);
            headersStata.add(stata);
        });

        // Headers Dinámicos (Crudos + Dicotomizados)
        for (CampoCrf campo : columnasDinamicas) {
            // Columna de Dato Crudo
            headersOriginal.add(campo.getNombre());
            headersStata.add(StataFormateador.formatarNombreVariable(campo.getNombre()));

            // Nuevas Columnas (Dicotomizadas)
            if (criteriosMap.containsKey(campo.getIdCampo())) {
                for (CriterioDTO criterio : criteriosMap.get(campo.getIdCampo())) {
                    String nombreColOriginal = excelReporteService.getNombreColumnaDicotomizada(campo.getNombre(), criterio);
                    headersOriginal.add(nombreColOriginal);
                    headersStata.add(StataFormateador.formatarNombreVariable(nombreColOriginal));
                }
            }
        }

        // --- 4. PROCESAR FILAS ---
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (CrfResumenRowDTO fila : filas) {
            Map<String, String> filaOrig = new LinkedHashMap<>();
            Map<String, String> filaStata = new LinkedHashMap<>();

            // Datos Fijos
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

            // Datos Dinámicos
            for (CampoCrf campo : columnasDinamicas) {
                Integer campoId = campo.getIdCampo();
                String valorCrudo = fila.getValores().get(campoId);
                valorCrudo = (valorCrudo != null) ? valorCrudo : "";
                
                String headerOrigCrudo = campo.getNombre();
                String headerStataCrudo = StataFormateador.formatarNombreVariable(headerOrigCrudo);
                
                filaOrig.put(headerOrigCrudo, valorCrudo);
                filaStata.put(headerStataCrudo, StataFormateador.formatarValor(valorCrudo));

                // Columnas Dicotomizadas
                if (criteriosMap.containsKey(campoId)) {
                    for (CriterioDTO criterio : criteriosMap.get(campoId)) {
                        int valorDicotomizado = excelReporteService.calcularValorDicotomizado(valorCrudo, criterio, puntosDeCorte.get(campoId + "_" + criterio.getTipo()));
                        String valorDicoStr = String.valueOf(valorDicotomizado);

                        String headerOrigDico = excelReporteService.getNombreColumnaDicotomizada(campo.getNombre(), criterio);
                        String headerStataDico = StataFormateador.formatarNombreVariable(headerOrigDico);
                        
                        filaOrig.put(headerOrigDico, valorDicoStr);
                        filaStata.put(headerStataDico, valorDicoStr); // El valor 0 o 1 no necesita formateo
                    }
                }
            }
            filasOriginal.add(filaOrig);
            filasStata.add(filaStata);
        }

        return new StataPreviewDTO(headersOriginal, headersStata, filasOriginal, filasStata);
    }
}