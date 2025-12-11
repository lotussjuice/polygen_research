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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CampoCrfStatsDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenRowDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CriterioDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.util.CalculoService;

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
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ByteArrayInputStream generarReporteDicotomizado(String criteriosJson) throws IOException {

        Map<Integer, List<CriterioDTO>> criteriosMap = parsearCriterios(criteriosJson);
        
        CrfResumenViewDTO data = crfService.getCrfResumenView(true); 

        List<CampoCrfStatsDTO> columnasStats = data.getCamposConStats(); 
        List<CrfResumenRowDTO> filas = data.getFilas();
        
        Map<String, Double> puntosDeCorte = preCalcularPuntosDeCorte(filas, criteriosMap, columnasStats);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte Dicotomizado");

            crearEncabezado(sheet.createRow(0), columnasStats, criteriosMap);

            int rowIdx = 1;
            for (CrfResumenRowDTO fila : filas) {
                llenarFila(sheet.createRow(rowIdx++), fila, columnasStats, criteriosMap, puntosDeCorte);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private Map<Integer, List<CriterioDTO>> parsearCriterios(String criteriosJson) throws IOException {
        try {
            TypeReference<HashMap<Integer, List<CriterioDTO>>> typeRef = new TypeReference<>() {
            };
            return objectMapper.readValue(criteriosJson, typeRef);
        } catch (IOException e) {
            throw new IOException("Error parseando JSON de criterios", e);
        }
    }

    public Map<String, Double> preCalcularPuntosDeCorte(List<CrfResumenRowDTO> filas,
            Map<Integer, List<CriterioDTO>> criteriosMap, List<CampoCrfStatsDTO> stats) {
        Map<String, Double> puntosDeCorte = new HashMap<>();

        for (Map.Entry<Integer, List<CriterioDTO>> entry : criteriosMap.entrySet()) {
            Integer campoId = entry.getKey();
            List<CriterioDTO> criterios = entry.getValue();

            boolean necesitaCalculo = criterios.stream()
                    .anyMatch(c -> "media".equals(c.getTipo()) || "promedio".equals(c.getTipo())
                            || "mediana".equals(c.getTipo()));

            if (necesitaCalculo) {
                String colKey = String.valueOf(campoId); 
                List<Double> valoresColumna = getValoresNumericos(filas, colKey);

                for (CriterioDTO criterio : criterios) {
                    String tipo = criterio.getTipo();
                    String clave = campoId + "_" + tipo;

                    if (!puntosDeCorte.containsKey(clave)) {
                        if ("media".equals(tipo) || "promedio".equals(tipo)) {
                            puntosDeCorte.put(clave, calculoService.calcularMedia(valoresColumna));
                        } else if ("mediana".equals(tipo)) {
                            puntosDeCorte.put(clave, calculoService.calcularMediana(valoresColumna));
                        }
                    }
                }
            }
        }
        return puntosDeCorte;
    }

    public String getNombreColumnaDicotomizada(String nombreCampo, CriterioDTO criterio) {
        if (criterio.getNombreColumna() != null && !criterio.getNombreColumna().isEmpty()) {
            return criterio.getNombreColumna();
        }
        if ("personalizado".equals(criterio.getTipo()) && criterio.getNombre() != null) {
            return nombreCampo + "_" + criterio.getNombre();
        }
        return nombreCampo + "_" + criterio.getTipo();
    }

    public int calcularValorDicotomizado(Map<String, String> valoresFila, double valorNumColumna, CriterioDTO criterio, double puntoCorteCalculado) {
        
        if (criterio.getReglas() != null && !criterio.getReglas().isEmpty()) {
            boolean cumpleTodas = true;
            for (CriterioDTO.ReglaDTO regla : criterio.getReglas()) {
                String keyCampo = String.valueOf(regla.getCampoId());
                String valorCelda = valoresFila.get(keyCampo);

                if (!evaluarReglaIndividual(valorCelda, regla.getOperador(), regla.getValor())) {
                    cumpleTodas = false;
                    break;
                }
            }
            return cumpleTodas ? 1 : 0;
        }

        if (criterio.getTipo() == null) return 0;

        double puntoCorte;
        return switch (criterio.getTipo()) {
            case "media", "promedio" -> {
                puntoCorte = puntoCorteCalculado;
                yield (valorNumColumna >= puntoCorte) ? 1 : 0;
            }
            case "mediana" -> {
                puntoCorte = puntoCorteCalculado;
                yield (valorNumColumna >= puntoCorte) ? 1 : 0;
            }
            case "personalizado" ->
                aplicarRegla(valorNumColumna, criterio.getPuntoCorte()) ? 1 : 0;
            default -> 0;
        };
    }

    private boolean evaluarReglaIndividual(String valorCelda, String operador, String valorRegla) {
        if (valorCelda == null || valorCelda.trim().isEmpty() || "-".equals(valorCelda)) return false;

        String vCelda = valorCelda.trim();
        String vRegla = valorRegla.trim();

        Double nCelda = null;
        Double nRegla = null;
        try {
            nCelda = Double.parseDouble(vCelda.replace(',', '.'));
            nRegla = Double.parseDouble(vRegla.replace(',', '.'));
        } catch (NumberFormatException e) {
        }

        boolean sonNumeros = (nCelda != null && nRegla != null);

        switch (operador) {
            case "==": return sonNumeros ? nCelda.equals(nRegla) : vCelda.equalsIgnoreCase(vRegla);
            case "!=": return sonNumeros ? !nCelda.equals(nRegla) : !vCelda.equalsIgnoreCase(vRegla);
            case ">":  return sonNumeros && nCelda > nRegla;
            case ">=": return sonNumeros && nCelda >= nRegla;
            case "<":  return sonNumeros && nCelda < nRegla;
            case "<=": return sonNumeros && nCelda <= nRegla;
            case "contains": return vCelda.toLowerCase().contains(vRegla.toLowerCase());
            default: return false;
        }
    }

    private void crearEncabezado(Row headerRow, List<CampoCrfStatsDTO> columnasStats,
            Map<Integer, List<CriterioDTO>> criteriosMap) {

        int cellIdx = 0;
        headerRow.createCell(cellIdx++).setCellValue("ID CRF");
        headerRow.createCell(cellIdx++).setCellValue("Cód. Paciente");

        for (CampoCrfStatsDTO stat : columnasStats) {
            CampoCrf campo = stat.getCampoCrf();
            headerRow.createCell(cellIdx++).setCellValue(stat.getNombreColumna());

            if (stat.getOpcion() == null && criteriosMap.containsKey(campo.getIdCampo())) {
                for (CriterioDTO criterio : criteriosMap.get(campo.getIdCampo())) {
                    String nombreColumna = getNombreColumnaDicotomizada(stat.getNombreColumna(), criterio);
                    headerRow.createCell(cellIdx++).setCellValue(nombreColumna);
                }
            }
        }
    }

    private void llenarFila(Row dataRow, CrfResumenRowDTO fila, List<CampoCrfStatsDTO> columnasStats,
            Map<Integer, List<CriterioDTO>> criteriosMap, Map<String, Double> puntosDeCorte) {

        int cellIdx = 0;
        dataRow.createCell(cellIdx++).setCellValue(fila.getCrf().getIdCrf());
        dataRow.createCell(cellIdx++).setCellValue(
                fila.getCrf().getDatosPaciente() != null ? fila.getCrf().getDatosPaciente().getCodigoPaciente() : "");
        
        for (CampoCrfStatsDTO stat : columnasStats) {
            CampoCrf campo = stat.getCampoCrf();
            String colKey = stat.getColumnaKey();
            String valorCrudo = fila.getValores().get(colKey);

            dataRow.createCell(cellIdx++).setCellValue(valorCrudo != null ? valorCrudo : "-");

            if (stat.getOpcion() == null && criteriosMap.containsKey(campo.getIdCampo())) {
                
                double valorNum = parseDouble(valorCrudo);

                for (CriterioDTO criterio : criteriosMap.get(campo.getIdCampo())) {
                    
                    double puntoCorteCalculado = 0.0;
                    if (criterio.getTipo() != null) {
                         puntoCorteCalculado = puntosDeCorte.getOrDefault(campo.getIdCampo() + "_" + criterio.getTipo(), 0.0);
                    }

                    int valorDicotomizado = calcularValorDicotomizado(fila.getValores(), valorNum, criterio, puntoCorteCalculado);
                    
                    dataRow.createCell(cellIdx++).setCellValue(valorDicotomizado);
                }
            }
        }
    }

    public double parseDouble(String valor) {
        if (valor == null || valor.trim().isEmpty() || valor.trim().equals("-")) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private List<Double> getValoresNumericos(List<CrfResumenRowDTO> filas, String colKey) {
        List<Double> valores = new ArrayList<>();
        for (CrfResumenRowDTO fila : filas) {
            String valorCrudo = fila.getValores().get(colKey);
            double valorNum = parseDouble(valorCrudo);
            if (!Double.isNaN(valorNum)) {
                valores.add(valorNum);
            }
        }
        return valores;
    }

    public boolean aplicarRegla(double valor, String regla) {
        if (regla == null || Double.isNaN(valor)) {
            return false;
        }
        try {
            Matcher matcher = REGLA_PATTERN.matcher(regla);
            if (!matcher.find()) {
                return false;
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
            return false;
        }
    }
}