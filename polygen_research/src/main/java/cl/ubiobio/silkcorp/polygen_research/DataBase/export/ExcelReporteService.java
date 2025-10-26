package cl.ubiobio.silkcorp.polygen_research.DataBase.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenRowDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CriterioDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.util.CalculoService;

@Service
public class ExcelReporteService {

    private final CrfService crfService;
    private final CalculoService calculoService;
    private final ObjectMapper objectMapper; // Para parsear el JSON

    public ExcelReporteService(CrfService crfService, CalculoService calculoService) {
        this.crfService = crfService;
        this.calculoService = calculoService;
        this.objectMapper = new ObjectMapper();
    }

    public ByteArrayInputStream generarReporteDicotomizado(String criteriosJson) throws IOException {

        // 1. PARSEAR LOS CRITERIOS JSON
        Map<Integer, List<CriterioDTO>> criteriosMap;
        try {
            TypeReference<HashMap<Integer, List<CriterioDTO>>> typeRef = new TypeReference<>() {};
            criteriosMap = objectMapper.readValue(criteriosJson, typeRef);
        } catch (IOException e) {
            throw new IOException("Error parseando JSON de criterios", e);
        }

        // 2. OBTENER LOS DATOS CRUDOS (tal como en la vista)
        CrfResumenViewDTO data = crfService.getCrfResumenView();
        List<CampoCrf> columnas = data.getCamposActivos();
        List<CrfResumenRowDTO> filas = data.getFilas();

        // 3. PRE-CALCULAR MEDIAS Y MEDIANAS
        Map<String, Double> puntosDeCorte = new HashMap<>();
        for (Map.Entry<Integer, List<CriterioDTO>> entry : criteriosMap.entrySet()) {
            Integer campoId = entry.getKey();
            List<CriterioDTO> criterios = entry.getValue();
            
            boolean necesitaCalculo = criterios.stream()
                .anyMatch(c -> c.getTipo().equals("media") || c.getTipo().equals("promedio") || c.getTipo().equals("mediana"));

            if (necesitaCalculo) {
                // Obtener todos los valores de esta columna
                List<Double> valoresColumna = getValoresNumericos(filas, campoId);
                
                for (CriterioDTO criterio : criterios) {
                    if (criterio.getTipo().equals("media") || criterio.getTipo().equals("promedio")) {
                        double media = calculoService.calcularMedia(valoresColumna);
                        puntosDeCorte.put(campoId + "_" + criterio.getTipo(), media);
                    } else if (criterio.getTipo().equals("mediana")) {
                        double mediana = calculoService.calcularMediana(valoresColumna);
                        puntosDeCorte.put(campoId + "_" + criterio.getTipo(), mediana);
                    }
                }
            }
        }

        // 4. CONSTRUIR EL EXCEL
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte Dicotomizado");
            
            // --- 4a. Crear Encabezado (Header) ---
            Row headerRow = sheet.createRow(0);
            int cellIdx = 0;
            
            // Columnas Fijas
            headerRow.createCell(cellIdx++).setCellValue("ID CRF");
            headerRow.createCell(cellIdx++).setCellValue("Cód. Paciente");
            headerRow.createCell(cellIdx++).setCellValue("Paciente");
            headerRow.createCell(cellIdx++).setCellValue("Fecha Creación");

            // Columnas Dinámicas (Crudas + Dicotomizadas)
            for (CampoCrf campo : columnas) {
                // Columna de Dato Crudo
                headerRow.createCell(cellIdx++).setCellValue(campo.getNombre());
                
                // Nuevas Columnas (Dicotomizadas)
                if (criteriosMap.containsKey(campo.getIdCampo())) {
                    for (CriterioDTO criterio : criteriosMap.get(campo.getIdCampo())) {
                        String nombreColumna = "";
                        if (criterio.getTipo().equals("personalizado")) {
                            nombreColumna = campo.getNombre() + "_" + criterio.getNombre();
                        } else {
                            nombreColumna = campo.getNombre() + "_" + criterio.getTipo();
                        }
                        headerRow.createCell(cellIdx++).setCellValue(nombreColumna);
                    }
                }
            }

            // --- 4b. Llenar Filas de Datos ---
            int rowIdx = 1;
            for (CrfResumenRowDTO fila : filas) {
                Row dataRow = sheet.createRow(rowIdx++);
                cellIdx = 0;
                
                // Datos Fijos
                dataRow.createCell(cellIdx++).setCellValue(fila.getCrf().getIdCrf());
                dataRow.createCell(cellIdx++).setCellValue(fila.getCrf().getDatosPaciente() != null ? fila.getCrf().getDatosPaciente().getCodigoPaciente() : "");
                String nombreCompleto = (fila.getCrf().getDatosPaciente() != null) ? fila.getCrf().getDatosPaciente().getNombre() + " " + fila.getCrf().getDatosPaciente().getApellido() : "N/A";
                dataRow.createCell(cellIdx++).setCellValue(nombreCompleto);
                dataRow.createCell(cellIdx++).setCellValue(fila.getCrf().getFechaConsulta() != null ? fila.getCrf().getFechaConsulta().toString() : "N/A");

                // Datos Dinámicos
                for (CampoCrf campo : columnas) {
                    Integer campoId = campo.getIdCampo();
                    String valorCrudo = fila.getValores().get(campoId);
                    
                    // Dato Crudo
                    dataRow.createCell(cellIdx++).setCellValue(valorCrudo != null ? valorCrudo : "-");
                    double valorNum = parseDouble(valorCrudo);

                    // Columnas Dicotomizadas
                    if (criteriosMap.containsKey(campoId)) {
                        for (CriterioDTO criterio : criteriosMap.get(campoId)) {
                            int valorDicotomizado = 0;
                            double puntoCorte;
                            
                            switch (criterio.getTipo()) {
                                case "media", "promedio" -> {
                                    puntoCorte = puntosDeCorte.get(campoId + "_" + criterio.getTipo());
                                    valorDicotomizado = (valorNum >= puntoCorte) ? 1 : 0; // 1 si es >= media, 0 si es <
                                }
                                case "mediana" -> {
                                    puntoCorte = puntosDeCorte.get(campoId + "_" + criterio.getTipo());
                                    valorDicotomizado = (valorNum >= puntoCorte) ? 1 : 0; // 1 si es >= mediana, 0 si es <
                                }
                                case "personalizado" -> valorDicotomizado = aplicarRegla(valorNum, criterio.getPuntoCorte()) ? 1 : 0; // 1 si cumple regla
                            }
                            dataRow.createCell(cellIdx++).setCellValue(valorDicotomizado);
                        }
                    }
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // --- Métodos de Ayuda ---
    private double parseDouble(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0.0; // O Double.NaN si lo prefieres
        }
        try {
            return Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Extrae todos los valores numéricos de una columna específica.
     */
    private List<Double> getValoresNumericos(List<CrfResumenRowDTO> filas, Integer campoId) {
        List<Double> valores = new ArrayList<>();
        for (CrfResumenRowDTO fila : filas) {
            String valorCrudo = fila.getValores().get(campoId);
            if (valorCrudo != null && !valorCrudo.trim().isEmpty()) {
                try {
                    valores.add(Double.valueOf(valorCrudo.trim()));
                } catch (NumberFormatException e) {
                    // Ignora valores no numéricos
                }
            }
        }
        return valores;
    }

    /**
     * Aplica una regla de dicotomización personalizada (ej: "<30", ">=50").
     */
    private boolean aplicarRegla(double valor, String regla) {
        try {
            // Usamos una expresión regular para extraer el operador y el número
            Pattern pattern = Pattern.compile("([<>=!]+)\\s*([0-9.-]+)");
            Matcher matcher = pattern.matcher(regla);
            
            if (!matcher.find()) {
                return false; // Regla mal formada
            }
            
            String operador = matcher.group(1);
            double puntoCorte = Double.parseDouble(matcher.group(2));

            return switch (operador) {
                case "<" -> valor < puntoCorte;
                case "<=" -> valor <= puntoCorte;
                case ">" -> valor > puntoCorte;
                case ">=" -> valor >= puntoCorte;
                case "==" -> valor == puntoCorte;
                case "!=" -> valor != puntoCorte;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return false; // Error al parsear
        }
    }
}