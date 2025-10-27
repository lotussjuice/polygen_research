package cl.ubiobio.silkcorp.polygen_research.DataBase.export;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenRowDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CriterioDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.util.CalculoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExcelReporteService {

    private final CrfService crfService;
    private final CalculoService calculoService;
    private final ObjectMapper objectMapper;

    private static final Pattern REGLA_PATTERN = Pattern.compile("([<>=!]+)\\s*([0-9.-]+)");

    public ExcelReporteService(CrfService crfService, CalculoService calculoService) {
        this.crfService = crfService;
        this.calculoService = calculoService;
        this.objectMapper = new ObjectMapper();
    }

    public ByteArrayInputStream generarReporteDicotomizado(String criteriosJson) throws IOException {

        Map<Integer, List<CriterioDTO>> criteriosMap = parsearCriterios(criteriosJson);
        CrfResumenViewDTO data = crfService.getCrfResumenView();
        List<CampoCrf> columnas = data.getCamposActivos();
        List<CrfResumenRowDTO> filas = data.getFilas();
        Map<String, Double> puntosDeCorte = preCalcularPuntosDeCorte(filas, criteriosMap);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte Dicotomizado");
            crearEncabezado(sheet.createRow(0), columnas, criteriosMap);

            int rowIdx = 1;
            for (CrfResumenRowDTO fila : filas) {
                llenarFila(sheet.createRow(rowIdx++), fila, columnas, criteriosMap, puntosDeCorte);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // --- MÉTODOS PÚBLICOS REUTILIZABLES ---

    private Map<Integer, List<CriterioDTO>> parsearCriterios(String criteriosJson) throws IOException {
        try {
            TypeReference<HashMap<Integer, List<CriterioDTO>>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(criteriosJson, typeRef);
        } catch (IOException e) {
            throw new IOException("Error parseando JSON de criterios", e);
        }
    }

    public Map<String, Double> preCalcularPuntosDeCorte(List<CrfResumenRowDTO> filas, Map<Integer, List<CriterioDTO>> criteriosMap) {
        Map<String, Double> puntosDeCorte = new HashMap<>();

        for (Map.Entry<Integer, List<CriterioDTO>> entry : criteriosMap.entrySet()) {
            Integer campoId = entry.getKey();
            List<CriterioDTO> criterios = entry.getValue();

            boolean necesitaCalculo = criterios.stream()
                    .anyMatch(c -> c.getTipo().equals("media") || c.getTipo().equals("promedio") || c.getTipo().equals("mediana"));

            if (necesitaCalculo) {
                // <-- CAMBIO 1: getValoresNumericos ahora usa la lógica de parseDouble corregida
                List<Double> valoresColumna = getValoresNumericos(filas, campoId);

                for (CriterioDTO criterio : criterios) {
                    String tipo = criterio.getTipo();
                    String clave = campoId + "_" + tipo;

                    if (!puntosDeCorte.containsKey(clave)) {
                        if (tipo.equals("media") || tipo.equals("promedio")) {
                            puntosDeCorte.put(clave, calculoService.calcularMedia(valoresColumna));
                        } else if (tipo.equals("mediana")) {
                            puntosDeCorte.put(clave, calculoService.calcularMediana(valoresColumna));
                        }
                    }
                }
            }
        }
        return puntosDeCorte;
    }

    public String getNombreColumnaDicotomizada(String nombreCampo, CriterioDTO criterio) {
        if ("personalizado".equals(criterio.getTipo())) {
            return nombreCampo + "_" + criterio.getNombre();
        } else {
            return nombreCampo + "_" + criterio.getTipo();
        }
    }

    /**
     * Calcula el valor dicotomizado (0 o 1) basado en el criterio y el valor crudo.
     * @param valorNum El valor numérico ya parseado.
     * @param criterio El DTO del criterio a aplicar.
     * @param puntoCorteCalculado El valor pre-calculado (media/mediana) si es necesario.
     */
    // <-- CAMBIO 2: La firma del método ahora acepta 'double valorNum' en lugar de 'String valorCrudo'
    public int calcularValorDicotomizado(double valorNum, CriterioDTO criterio, double puntoCorteCalculado) {
        // El parseo ya se hizo en llenarFila.
        // La comprobación de NaN también se hizo en llenarFila.
        double puntoCorte;

        return switch (criterio.getTipo()) {
            case "media", "promedio" -> {
                puntoCorte = puntoCorteCalculado; // Usa el valor pre-calculado
                yield (valorNum >= puntoCorte) ? 1 : 0;
            }
            case "mediana" -> {
                puntoCorte = puntoCorteCalculado; // Usa el valor pre-calculado
                yield (valorNum >= puntoCorte) ? 1 : 0;
            }
            case "personalizado" ->
                // El puntoCorte está dentro del DTO (ej: ">=30")
                aplicarRegla(valorNum, criterio.getPuntoCorte()) ? 1 : 0;
            default -> 0;
        };
    }

    // --- MÉTODOS PRIVADOS DE AYUDA (INTERNOS) ---

    private void crearEncabezado(Row headerRow, List<CampoCrf> columnas, Map<Integer, List<CriterioDTO>> criteriosMap) {
        int cellIdx = 0;
        headerRow.createCell(cellIdx++).setCellValue("ID CRF");
        headerRow.createCell(cellIdx++).setCellValue("Cód. Paciente");
        // (Columnas de Paciente y Fecha eliminadas del HTML, las quitamos aquí también)
        // headerRow.createCell(cellIdx++).setCellValue("Paciente");
        // headerRow.createCell(cellIdx++).setCellValue("Fecha Creación");

        for (CampoCrf campo : columnas) {
            headerRow.createCell(cellIdx++).setCellValue(campo.getNombre());
            if (criteriosMap.containsKey(campo.getIdCampo())) {
                for (CriterioDTO criterio : criteriosMap.get(campo.getIdCampo())) {
                    String nombreColumna = getNombreColumnaDicotomizada(campo.getNombre(), criterio);
                    headerRow.createCell(cellIdx++).setCellValue(nombreColumna);
                }
            }
        }
    }

    private void llenarFila(Row dataRow, CrfResumenRowDTO fila, List<CampoCrf> columnas, Map<Integer, List<CriterioDTO>> criteriosMap, Map<String, Double> puntosDeCorte) {
        int cellIdx = 0;

        // Datos Fijos
        dataRow.createCell(cellIdx++).setCellValue(fila.getCrf().getIdCrf());
        dataRow.createCell(cellIdx++).setCellValue(fila.getCrf().getDatosPaciente() != null ? fila.getCrf().getDatosPaciente().getCodigoPaciente() : "");
        // (Columnas de Paciente y Fecha eliminadas del HTML, las quitamos aquí también)
        // String nombreCompleto = ...
        // dataRow.createCell(cellIdx++).setCellValue(nombreCompleto);
        // dataRow.createCell(cellIdx++).setCellValue(fila.getCrf().getFechaConsulta() != null ? ...);

        // Datos Dinámicos
        for (CampoCrf campo : columnas) {
            Integer campoId = campo.getIdCampo();
            String valorCrudo = fila.getValores().get(campoId);

            // Dato Crudo
            dataRow.createCell(cellIdx++).setCellValue(valorCrudo != null ? valorCrudo : "-");
            
            // <-- CAMBIO 3: Parseamos el valor UNA SOLA VEZ
            double valorNum = parseDouble(valorCrudo);

            // Columnas Dicotomizadas
            if (criteriosMap.containsKey(campoId)) {
                for (CriterioDTO criterio : criteriosMap.get(campoId)) {
                    
                    // --- ¡ESTA ES LA LÓGICA CLAVE QUE SOLICITASTE! ---
                    if (Double.isNaN(valorNum)) {
                        // Si el valor crudo no es un número, 
                        // la celda dicotomizada debe ir vacía.
                        dataRow.createCell(cellIdx++).setCellValue(""); // <-- Celda vacía
                    } else {
                        // Si es un número válido, procede con el cálculo
                        double puntoCorteCalculado = puntosDeCorte.getOrDefault(campoId + "_" + criterio.getTipo(), 0.0);
                        // Pasamos el 'valorNum' ya parseado
                        int valorDicotomizado = calcularValorDicotomizado(valorNum, criterio, puntoCorteCalculado);
                        dataRow.createCell(cellIdx++).setCellValue(valorDicotomizado);
                    }
                }
            }
        }
    }

    /**
     * Parsea un String a double de forma segura.
     * Devuelve Double.NaN si el valor es nulo, vacío, "-", o no numérico.
     */
    public double parseDouble(String valor) {
        // <-- CAMBIO 4: Lógica de parseo mejorada
        if (valor == null || valor.trim().isEmpty() || valor.trim().equals("-")) {
            return Double.NaN;
        }
        try {
            // Reemplaza comas por puntos para decimales
            return Double.parseDouble(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            // Si es texto (ej: "N/A"), también es NaN
            return Double.NaN;
        }
    }

    /**
     * Extrae todos los valores numéricos de una columna específica.
     * (Modificado para usar el nuevo parseDouble)
     */
    private List<Double> getValoresNumericos(List<CrfResumenRowDTO> filas, Integer campoId) {
        List<Double> valores = new ArrayList<>();
        for (CrfResumenRowDTO fila : filas) {
            String valorCrudo = fila.getValores().get(campoId);
            
            // Usamos el parseDouble unificado
            double valorNum = parseDouble(valorCrudo); 
            
            // Solo añadimos si es un número válido
            if (!Double.isNaN(valorNum)) {
                valores.add(valorNum);
            }
        }
        return valores;
    }

    /**
     * Aplica una regla de dicotomización personalizada (ej: "<30", ">=50").
     */
    public boolean aplicarRegla(double valor, String regla) {
        // Añadida comprobación de NaN y nulo
        if (regla == null || Double.isNaN(valor)) {
            return false;
        }
        try {
            Matcher matcher = REGLA_PATTERN.matcher(regla);
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
        } catch (NumberFormatException | IllegalStateException e) {
            return false; // Error al parsear o encontrar el patrón
        }
    }
}