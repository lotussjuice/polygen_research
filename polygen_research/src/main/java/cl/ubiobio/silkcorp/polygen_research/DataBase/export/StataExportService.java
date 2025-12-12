package cl.ubiobio.silkcorp.polygen_research.DataBase.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

@Service
public class StataExportService {

    private final CrfService crfService;
    private final ExcelReporteService excelReporteService;
    private final ObjectMapper objectMapper;

    // Patrones precompilados para rendimiento
    private static final Pattern PATTERN_ACENTOS = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern PATTERN_CARACTERES_INVALIDOS = Pattern.compile("[^a-zA-Z0-9_]");
    private static final Pattern PATTERN_INICIO_NUMERO = Pattern.compile("^[0-9]");
    private static final Pattern PATTERN_SALTO_LINEA = Pattern.compile("[\r\n]+");

    public StataExportService(CrfService crfService, CalculoService calculoService,
            ExcelReporteService excelReporteService) {
        this.crfService = crfService;
        this.excelReporteService = excelReporteService;
        this.objectMapper = new ObjectMapper();
    }

    public ByteArrayInputStream generarReporteStata(String criteriosJson) throws IOException {
        StataPreviewDTO data = prepararDatosStata(criteriosJson);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Datos_STATA");

            // Header
            Row headerRow = sheet.createRow(0);
            List<String> headers = data.getHeadersStata();
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }

            // Datos
            int rowIdx = 1;
            List<Map<String, String>> filas = data.getFilasStata();

            for (Map<String, String> fila : filas) {
                Row dataRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i);
                    String valor = fila.get(header);

                    Cell cell = dataRow.createCell(i);
                    
                    if (valor == null || valor.isEmpty()) {
                        cell.setBlank(); // Nulo real para Stata
                    } else {
                        // Intentar guardar como número si es posible
                        try {
                            double valNum = Double.parseDouble(valor.replace(',', '.'));
                            // Si es entero (ej: 1.0), guardar sin decimales visualmente si se desea, 
                            // pero POI maneja double.
                            cell.setCellValue(valNum);
                        } catch (NumberFormatException e) {
                            // Es texto, limpiar caracteres conflictivos para Stata
                            cell.setCellValue(limpiarTextoStata(valor));
                        }
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
        CrfResumenViewDTO data = crfService.getCrfResumenView(true);
        List<CampoCrfStatsDTO> columnasDinamicasStats = data.getCamposConStats();
        List<CrfResumenRowDTO> filas = data.getFilas();

        Map<Integer, List<CriterioDTO>> criteriosMap;
        try {
            if (criteriosJson != null && !criteriosJson.isEmpty() && !criteriosJson.equals("{}")) {
                criteriosMap = objectMapper.readValue(criteriosJson, new TypeReference<HashMap<Integer, List<CriterioDTO>>>() {});
            } else {
                criteriosMap = new HashMap<>();
            }
        } catch (IOException e) {
            criteriosMap = new HashMap<>(); // Fallback seguro
        }

        Map<String, Double> puntosDeCorte = excelReporteService.preCalcularPuntosDeCorte(filas, criteriosMap, columnasDinamicasStats);

        List<String> headersOriginal = new ArrayList<>();
        List<String> headersStata = new ArrayList<>();
        List<Map<String, String>> filasOriginal = new ArrayList<>();
        List<Map<String, String>> filasStata = new ArrayList<>();

        // Headers Fijos
        headersOriginal.add("ID_CRF");
        headersStata.add("id_crf");
        headersOriginal.add("Codigo_Paciente");
        headersStata.add("cod_paciente");

        // Headers Dinámicos
        for (CampoCrfStatsDTO stat : columnasDinamicasStats) {
            CampoCrf campo = stat.getCampoCrf();
            String tipo = campo.getTipo();

            if (!"NUMERO".equals(tipo) && !"SI/NO".equals(tipo) && !"SELECCION_UNICA".equals(tipo)
                && !"TEXTO".equals(tipo) && !"FECHA".equals(tipo)) { 
                continue;
            }

            String nombreReal = stat.getNombreColumna();
            headersOriginal.add(nombreReal);
            headersStata.add(sanitizarNombreVariable(nombreReal));

            // Columnas Calculadas (Criterios)
            if (criteriosMap.containsKey(campo.getIdCampo())) {
                for (CriterioDTO criterio : criteriosMap.get(campo.getIdCampo())) {
                    String nombreColDico = excelReporteService.getNombreColumnaDicotomizada(nombreReal, criterio);
                    headersOriginal.add(nombreColDico);
                    headersStata.add(sanitizarNombreVariable(nombreColDico));
                }
            }
        }

        // Procesar Filas
        for (CrfResumenRowDTO fila : filas) {
            Map<String, String> filaOrig = new LinkedHashMap<>();
            Map<String, String> filaStata = new LinkedHashMap<>();

            String idCrf = String.valueOf(fila.getCrf().getIdCrf());
            String codPac = fila.getCrf().getDatosPaciente() != null ? fila.getCrf().getDatosPaciente().getCodigoPaciente() : "";

            filaOrig.put("ID_CRF", idCrf);
            filaStata.put("id_crf", idCrf);
            
            filaOrig.put("Codigo_Paciente", codPac);
            filaStata.put("cod_paciente", limpiarTextoStata(codPac));

            for (CampoCrfStatsDTO stat : columnasDinamicasStats) {
                CampoCrf campo = stat.getCampoCrf();
                String tipo = campo.getTipo();

                if (!"NUMERO".equals(tipo) && !"SI/NO".equals(tipo) && !"SELECCION_UNICA".equals(tipo)
                    && !"TEXTO".equals(tipo) && !"FECHA".equals(tipo)) {
                    continue;
                }

                String colKey = stat.getColumnaKey();
                String valorCrudo = fila.getValores().get(colKey);
                // Si es nulo o guión, lo dejamos nulo para Stata (celda vacía)
                String valorStata = (valorCrudo == null || valorCrudo.equals("-") || valorCrudo.trim().isEmpty()) ? null : valorCrudo.trim();

                String headerOrig = stat.getNombreColumna();
                String headerStata = sanitizarNombreVariable(headerOrig);

                filaOrig.put(headerOrig, valorCrudo);
                filaStata.put(headerStata, valorStata);

                // Calculados
                if (criteriosMap.containsKey(campo.getIdCampo())) {
                    double valorNum = excelReporteService.parseDouble(valorCrudo);
                    // Si valorNum es NaN (era nulo), el resultado también debe ser nulo

                    for (CriterioDTO criterio : criteriosMap.get(campo.getIdCampo())) {
                        double puntoCorte = 0.0;
                        if(criterio.getTipo() != null) {
                            puntoCorte = puntosDeCorte.getOrDefault(campo.getIdCampo() + "_" + criterio.getTipo(), 0.0);
                        }

                        Integer valorDicotomizado = excelReporteService.calcularValorDicotomizado(fila.getValores(), valorNum, criterio, puntoCorte);
                        String valorDicoStr = (valorDicotomizado != null) ? String.valueOf(valorDicotomizado) : null;

                        String headerOrigDico = excelReporteService.getNombreColumnaDicotomizada(headerOrig, criterio);
                        String headerStataDico = sanitizarNombreVariable(headerOrigDico);

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

    /**
     * Limpia el nombre de la variable según las reglas estrictas de STATA:
     * - Max 32 chars
     * - Solo a-z, 0-9, _
     * - No empezar con número
     * - Sin espacios ni acentos
     */
    private String sanitizarNombreVariable(String nombreOriginal) {
        if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) {
            return "var_sin_nombre";
        }
        
        // 1. Normalizar (eliminar acentos)
        String nombre = Normalizer.normalize(nombreOriginal.trim(), Normalizer.Form.NFD);
        nombre = PATTERN_ACENTOS.matcher(nombre).replaceAll("");
        
        // 2. Reemplazar espacios y caracteres inválidos por guión bajo
        nombre = nombre.replaceAll("\\s+", "_"); // Espacios a _
        nombre = PATTERN_CARACTERES_INVALIDOS.matcher(nombre).replaceAll("_"); // Símbolos a _
        
        // 3. Eliminar guiones bajos duplicados o al inicio/fin
        nombre = nombre.replaceAll("_{2,}", "_");
        nombre = nombre.replaceAll("^_+|_+$", "");
        
        // 4. Asegurar que no empiece con número
        if (PATTERN_INICIO_NUMERO.matcher(nombre).find() || nombre.isEmpty()) {
            nombre = "v_" + nombre;
        }
        
        // 5. Todo a minúsculas
        nombre = nombre.toLowerCase();
        
        // 6. Truncar a 32 caracteres (límite de versiones antiguas de Stata, seguro para compatibilidad)
        if (nombre.length() > 32) {
            nombre = nombre.substring(0, 32);
        }
        
        return nombre;
    }

    /**
     * Limpia el contenido de texto para evitar errores de importación:
     * - Elimina saltos de línea
     * - Reemplaza comillas dobles
     */
    private String limpiarTextoStata(String texto) {
        if (texto == null) return null;
        String limpio = PATTERN_SALTO_LINEA.matcher(texto).replaceAll(" ");
        limpio = limpio.replace("\"", "'"); // Comillas dobles a simples para no romper CSV/String
        return limpio.trim();
    }
}