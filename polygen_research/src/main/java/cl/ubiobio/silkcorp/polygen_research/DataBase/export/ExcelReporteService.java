package cl.ubiobio.silkcorp.polygen_research.DataBase.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
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
import cl.ubiobio.silkcorp.polygen_research.DataBase.OpcionCampoCrf.OpcionCampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CampoCrfStatsDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenRowDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.util.CalculoService;

@Service
public class ExcelReporteService {

    private final CrfService crfService;
    private final CalculoService calculoService;
    private final ObjectMapper objectMapper;

    public ExcelReporteService(CrfService crfService, CalculoService calculoService) {
        this.crfService = crfService;
        this.calculoService = calculoService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public double parseDouble(String valor) {
        if (valor == null || valor.trim().isEmpty() || valor.equals("-")) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    // --- LOGICA EXCEL ---
    public ByteArrayInputStream generarReporteDicotomizado(String criteriosJson, String excludedColsJson) throws IOException {
        Map<String, CriterioFrontDTO> criteriosMap = parsearCriterios(criteriosJson);
        Set<String> columnasExcluidas = parsearExcluidas(excludedColsJson);

        CrfResumenViewDTO data = crfService.getCrfResumenView(true); // Trae SOLO activos
        List<CampoCrfStatsDTO> columnasOriginales = data.getCamposConStats();
        List<CrfResumenRowDTO> filas = data.getFilas();

        // 1. CREAR SET DE IDs ACTIVOS PARA VALIDACIÓN RÁPIDA
        Set<String> idsCamposActivos = columnasOriginales.stream()
                .map(c -> String.valueOf(c.getCampoCrf().getIdCampo()))
                .collect(Collectors.toSet());

        // 2. FILTRAR CRITERIOS: Solo procesar aquellos cuyos campos origen sigan activos
        List<String> uuidsOrdenados = new ArrayList<>();
        for (String uuid : criteriosMap.keySet()) {
            CriterioFrontDTO crit = criteriosMap.get(uuid);
            if (esCriterioValido(crit, idsCamposActivos)) {
                uuidsOrdenados.add(uuid);
            }
        }

        // 3. Pre-Calcular Medias y Medianas (Solo para los validados)
        Map<String, Double> puntosCorteCache = new HashMap<>();
        for (String uuid : uuidsOrdenados) {
            CriterioFrontDTO crit = criteriosMap.get(uuid);
            if (("media".equals(crit.getTipo()) || "mediana".equals(crit.getTipo())) && crit.getOrigenId() != null) {
                List<Double> vals = new ArrayList<>();
                String colKey = crit.getOrigenId(); 
                CampoCrfStatsDTO colStat = columnasOriginales.stream()
                    .filter(c -> String.valueOf(c.getCampoCrf().getIdCampo()).equals(colKey))
                    .findFirst().orElse(null);

                if (colStat != null) {
                    for (CrfResumenRowDTO f : filas) {
                        Double v = obtenerValorNumerico(f.getValores().get(colStat.getColumnaKey()), colStat.getCampoCrf());
                        if (v != null && !Double.isNaN(v)) vals.add(v);
                    }
                    if ("media".equals(crit.getTipo())) puntosCorteCache.put(uuid, calculoService.calcularMedia(vals));
                    else puntosCorteCache.put(uuid, calculoService.calcularMediana(vals));
                }
            }
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Datos CRF Numéricos");
            
            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            CellStyle calcHeaderStyle = workbook.createCellStyle();
            calcHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            calcHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            calcHeaderStyle.setFont(font);

            // Header
            Row headerRow = sheet.createRow(0);
            int colIdx = 0;
            crearCeldaHeader(headerRow, colIdx++, "ID_CRF", headerStyle);
            crearCeldaHeader(headerRow, colIdx++, "Cod_Paciente", headerStyle);

            List<CampoCrfStatsDTO> columnasAExportar = new ArrayList<>();
            for (CampoCrfStatsDTO stat : columnasOriginales) {
                String idStr = String.valueOf(stat.getCampoCrf().getIdCampo());
                String tipo = stat.getCampoCrf().getTipo();

                // FILTRO: Excluir TEXTO y FECHA explícitamente
                if (!columnasExcluidas.contains(idStr) && !"TEXTO".equals(tipo) && !"FECHA".equals(tipo)) {
                    crearCeldaHeader(headerRow, colIdx++, stat.getNombreColumna(), headerStyle);
                    columnasAExportar.add(stat);
                }
            }

            // SOLO AGREGAMOS HEADERS DE CRITERIOS VÁLIDOS
            for (String uuid : uuidsOrdenados) {
                CriterioFrontDTO criterio = criteriosMap.get(uuid);
                crearCeldaHeader(headerRow, colIdx++, criterio.getNombreColumna(), calcHeaderStyle);
            }

            // Datos
            int rowIdx = 1;
            for (CrfResumenRowDTO fila : filas) {
                Row row = sheet.createRow(rowIdx++);
                int cellIdx = 0;
                row.createCell(cellIdx++).setCellValue(fila.getCrf().getIdCrf());
                String cod = fila.getCrf().getDatosPaciente() != null ? fila.getCrf().getDatosPaciente().getCodigoPaciente() : "";
                row.createCell(cellIdx++).setCellValue(cod);

                // Columnas Originales (FORZAR NÚMERO)
                for (CampoCrfStatsDTO stat : columnasAExportar) {
                    String valTexto = fila.getValores().get(stat.getColumnaKey());
                    Double valNum = obtenerValorNumerico(valTexto, stat.getCampoCrf());
                    
                    Cell cell = row.createCell(cellIdx++);
                    if (valNum != null && !Double.isNaN(valNum)) {
                        cell.setCellValue(valNum); 
                    } else {
                        cell.setBlank(); 
                    }
                }

                // Columnas Calculadas (Solo las validadas)
                for (String uuid : uuidsOrdenados) {
                    CriterioFrontDTO criterio = criteriosMap.get(uuid);
                    Double puntoCorte = puntosCorteCache.get(uuid);
                    Integer resultado = evaluarCriterioFront(fila, criterio, data.getCamposConStats(), puntoCorte);
                    
                    Cell cell = row.createCell(cellIdx++);
                    if (resultado != null) {
                        cell.setCellValue(resultado); 
                    } else {
                        cell.setBlank(); 
                    }
                }
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // --- NUEVO: MÉTODO DE VALIDACIÓN ---
    private boolean esCriterioValido(CriterioFrontDTO crit, Set<String> idsActivos) {
        if ("complejo".equals(crit.getTipo())) {
            // Verificar que TODOS los campos usados en los bloques existan
            if (crit.getBloques() == null) return false;
            for (BloqueDTO bloque : crit.getBloques()) {
                if (bloque.getRules() == null) continue;
                for (ReglaDTO regla : bloque.getRules()) {
                    if (!idsActivos.contains(regla.getCampoId())) {
                        return false; // Una regla usa un campo inactivo -> inválido
                    }
                }
            }
            return true;
        } else {
            // Simple: Verificar origenId
            return idsActivos.contains(crit.getOrigenId());
        }
    }

    private Integer evaluarCriterioFront(CrfResumenRowDTO fila, CriterioFrontDTO criterio, List<CampoCrfStatsDTO> allStats, Double puntoCortePrecalc) {
        // --- LOGICA SIMPLE ---
        if ("media".equals(criterio.getTipo()) || "mediana".equals(criterio.getTipo()) || "personalizado".equals(criterio.getTipo())) {
            if (criterio.getOrigenId() != null) {
                CampoCrfStatsDTO stat = allStats.stream()
                    .filter(s -> String.valueOf(s.getCampoCrf().getIdCampo()).equals(criterio.getOrigenId()))
                    .findFirst().orElse(null);
                
                if (stat != null) {
                    String valTexto = fila.getValores().get(stat.getColumnaKey());
                    Double valNum = obtenerValorNumerico(valTexto, stat.getCampoCrf());
                    
                    if (valNum == null || Double.isNaN(valNum)) return null;

                    double target;
                    if ("personalizado".equals(criterio.getTipo())) {
                        target = parseDouble(criterio.getPuntoCorte());
                        if(Double.isNaN(target)) target = 0.0;
                    } else {
                        target = puntoCortePrecalc != null ? puntoCortePrecalc : 0.0;
                    }

                    String op = criterio.getOperador() != null ? criterio.getOperador() : ">";
                    return evaluarOperacionSimple(valNum, op, target) ? 1 : 0;
                }
            }
            return null; 
        } 
        
        // --- LOGICA COMPLEJA ---
        else if ("complejo".equals(criterio.getTipo()) && criterio.getBloques() != null) {
            
            boolean isMissingData = false;

            List<Boolean> blockResults = new ArrayList<>();
            for (BloqueDTO bloque : criterio.getBloques()) {
                
                List<Boolean> rulesResults = new ArrayList<>();
                for (ReglaDTO regla : bloque.getRules()) {
                    CampoCrfStatsDTO campoStat = allStats.stream()
                        .filter(s -> String.valueOf(s.getCampoCrf().getIdCampo()).equals(regla.getCampoId()))
                        .findFirst().orElse(null);

                    if (campoStat != null) {
                        String valTexto = fila.getValores().get(campoStat.getColumnaKey());
                        Double valNum = obtenerValorNumerico(valTexto, campoStat.getCampoCrf());
                        
                        if ((valNum == null || Double.isNaN(valNum)) && (valTexto == null || valTexto.equals("-") || valTexto.isEmpty())) {
                            isMissingData = true;
                        }

                        rulesResults.add(evaluarRegla(valNum, valTexto, regla.getOperador(), regla.getValor()));
                    } else {
                        isMissingData = true; 
                        rulesResults.add(false);
                    }
                }
                
                boolean blockResult;
                if ("AND".equals(bloque.getLogic())) blockResult = rulesResults.stream().allMatch(b -> b);
                else blockResult = rulesResults.stream().anyMatch(b -> b);
                blockResults.add(blockResult);
            }

            if (isMissingData) return null;

            if ("AND".equals(criterio.getGlobalLogic())) return blockResults.stream().allMatch(b -> b) ? 1 : 0;
            else return blockResults.stream().anyMatch(b -> b) ? 1 : 0;
        }
        return null;
    }

    private boolean evaluarOperacionSimple(double val, String op, double target) {
        switch (op) {
            case ">": return val > target;
            case ">=": return val >= target;
            case "<": return val < target;
            case "<=": return val <= target;
            case "==": return Math.abs(val - target) < 0.0001;
            case "!=": return Math.abs(val - target) > 0.0001;
            default: return false;
        }
    }

    private Double obtenerValorNumerico(String valorTexto, CampoCrf campo) {
        if (valorTexto == null || valorTexto.trim().isEmpty() || valorTexto.equals("-")) return null;
        String val = valorTexto.trim();
        String tipo = campo.getTipo();
        try {
            if ("NUMERO".equalsIgnoreCase(tipo)) return Double.parseDouble(val.replace(',', '.'));
            else if ("SI/NO".equalsIgnoreCase(tipo)) return val.equalsIgnoreCase("sí") || val.equals("1") || val.equalsIgnoreCase("si") ? 1.0 : 0.0;
            else if ("SELECCION_UNICA".equalsIgnoreCase(tipo)) {
                if (campo.getOpciones() != null) {
                    for (OpcionCampoCrf op : campo.getOpciones()) {
                        if (String.valueOf(op.getOrden()).equals(val) || op.getEtiqueta().equalsIgnoreCase(val)) 
                            return Double.valueOf(op.getOrden());
                    }
                }
                try { return Double.parseDouble(val); } catch(Exception e) { return null; }
            }
        } catch (Exception e) { return null; }
        return null;
    }

    private boolean evaluarRegla(Double valNum, String valTexto, String op, String target) {
        if (valNum == null && (valTexto == null || "contains".equals(op))) return false;
        
        Double targetNum = null;
        try { targetNum = Double.parseDouble(target); } catch(Exception e){}

        if (targetNum != null && valNum != null) {
            return evaluarOperacionSimple(valNum, op, targetNum);
        } else if (valTexto != null) {
            if ("contains".equals(op)) return valTexto.toLowerCase().contains(target.toLowerCase());
            if ("==".equals(op)) return valTexto.equalsIgnoreCase(target);
            if ("!=".equals(op)) return !valTexto.equalsIgnoreCase(target);
        }
        return false;
    }

    private void crearCeldaHeader(Row row, int idx, String texto, CellStyle style) {
        Cell cell = row.createCell(idx);
        cell.setCellValue(texto);
        cell.setCellStyle(style);
    }

    private Map<String, CriterioFrontDTO> parsearCriterios(String json) {
        try { return objectMapper.readValue(json, new TypeReference<HashMap<String, CriterioFrontDTO>>(){}); } 
        catch(Exception e) { return new HashMap<>(); }
    }

    private Set<String> parsearExcluidas(String json) {
        try { return objectMapper.readValue(json, new TypeReference<HashSet<String>>(){}); } 
        catch(Exception e) { return new HashSet<>(); }
    }

    public static class CriterioFrontDTO {
        public String nombreColumna;
        public String tipo;
        public String globalLogic;
        public List<BloqueDTO> bloques;
        public String puntoCorte;
        public String operador;
        public String origenId; 

        public String getNombreColumna() { return nombreColumna; }
        public String getTipo() { return tipo; }
        public String getGlobalLogic() { return globalLogic; }
        public List<BloqueDTO> getBloques() { return bloques; }
        public String getPuntoCorte() { return puntoCorte; }
        public String getOperador() { return operador; }
        public String getOrigenId() { return origenId; }
    }

    public static class BloqueDTO {
        public String logic;
        public List<ReglaDTO> rules;
        public String getLogic() { return logic; }
        public List<ReglaDTO> getRules() { return rules; }
    }

    public static class ReglaDTO {
        public String campoId;
        public String operador;
        public String valor;
        public String getCampoId() { return campoId; }
        public String getOperador() { return operador; }
        public String getValor() { return valor; }
    }
}