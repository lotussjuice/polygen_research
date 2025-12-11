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

@Service
public class ExcelReporteService {

    private final CrfService crfService;
    private final ObjectMapper objectMapper;

    public ExcelReporteService(CrfService crfService) {
        this.crfService = crfService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ByteArrayInputStream generarReporteDicotomizado(String criteriosJson, String excludedColsJson) throws IOException {
        
        // 1. Parsear JSONs del frontend
        Map<String, CriterioFrontDTO> criteriosMap = parsearCriterios(criteriosJson);
        Set<String> columnasExcluidas = parsearExcluidas(excludedColsJson);

        // 2. Obtener Datos
        CrfResumenViewDTO data = crfService.getCrfResumenView(true);
        List<CampoCrfStatsDTO> columnasOriginales = data.getCamposConStats();
        List<CrfResumenRowDTO> filas = data.getFilas();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Datos CRF Numéricos");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Estilo para columnas calculadas (Azul claro)
            CellStyle calcHeaderStyle = workbook.createCellStyle();
            calcHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            calcHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            calcHeaderStyle.setFont(font);

            // --- HEADER ---
            Row headerRow = sheet.createRow(0);
            int colIdx = 0;

            // 1. Estáticos
            crearCeldaHeader(headerRow, colIdx++, "ID CRF", headerStyle);
            crearCeldaHeader(headerRow, colIdx++, "Cód. Paciente", headerStyle);

            // 2. Columnas Originales (Filtrando excluidas)
            List<CampoCrfStatsDTO> columnasAExportar = new ArrayList<>();
            for (CampoCrfStatsDTO stat : columnasOriginales) {
                String idStr = String.valueOf(stat.getCampoCrf().getIdCampo());
                if (!columnasExcluidas.contains(idStr)) {
                    crearCeldaHeader(headerRow, colIdx++, stat.getNombreColumna(), headerStyle);
                    columnasAExportar.add(stat);
                }
            }

            // 3. Columnas Calculadas (Nuevas variables)
            List<String> uuidsOrdenados = new ArrayList<>(criteriosMap.keySet());
            for (String uuid : uuidsOrdenados) {
                CriterioFrontDTO criterio = criteriosMap.get(uuid);
                crearCeldaHeader(headerRow, colIdx++, criterio.getNombreColumna(), calcHeaderStyle);
            }

            // --- DATA ROWS ---
            int rowIdx = 1;
            for (CrfResumenRowDTO fila : filas) {
                Row row = sheet.createRow(rowIdx++);
                int cellIdx = 0;

                // 1. Estáticos
                row.createCell(cellIdx++).setCellValue(fila.getCrf().getIdCrf());
                String cod = fila.getCrf().getDatosPaciente() != null ? fila.getCrf().getDatosPaciente().getCodigoPaciente() : "";
                row.createCell(cellIdx++).setCellValue(cod);

                // 2. Columnas Originales (Convertidas a Número)
                for (CampoCrfStatsDTO stat : columnasAExportar) {
                    String valTexto = fila.getValores().get(stat.getColumnaKey());
                    Double valNum = obtenerValorNumerico(valTexto, stat.getCampoCrf());
                    
                    Cell cell = row.createCell(cellIdx++);
                    if (valNum != null) {
                        cell.setCellValue(valNum);
                    } else {
                        // Si no es número (ej: texto libre), poner texto, o vacío si es null
                        if(valTexto != null && !valTexto.equals("-")) cell.setCellValue(valTexto);
                        else cell.setBlank();
                    }
                }

                // 3. Columnas Calculadas
                for (String uuid : uuidsOrdenados) {
                    CriterioFrontDTO criterio = criteriosMap.get(uuid);
                    int resultado = evaluarCriterio(fila, criterio, data.getCamposConStats());
                    row.createCell(cellIdx++).setCellValue(resultado);
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * Convierte el valor de texto (Frontend) a valor numérico real para el Excel y cálculos.
     */
    private Double obtenerValorNumerico(String valorTexto, CampoCrf campo) {
        if (valorTexto == null || valorTexto.trim().isEmpty() || valorTexto.equals("-")) {
            return null;
        }

        String val = valorTexto.trim();
        String tipo = campo.getTipo();

        try {
            if ("NUMERO".equalsIgnoreCase(tipo)) {
                return Double.parseDouble(val.replace(',', '.'));
            } 
            else if ("SI/NO".equalsIgnoreCase(tipo)) {
                val = val.toLowerCase();
                if (val.equals("sí") || val.equals("si") || val.equals("yes") || val.equals("s") || val.equals("1")) return 1.0;
                if (val.equals("no") || val.equals("n") || val.equals("0")) return 0.0;
            } 
            else if ("SELECCION_UNICA".equalsIgnoreCase(tipo)) {
                // El valor en 'val' es el TEXTO de la opción (ej: "Fumador"). Buscamos su ID/Orden.
                if (campo.getOpciones() != null) {
                    for (OpcionCampoCrf op : campo.getOpciones()) {
                        if (op.getEtiqueta().equalsIgnoreCase(val)) {
                            // Retornamos el valor numérico de la opción (usualmente 'orden' o 'id')
                            return Double.valueOf(op.getOrden()); 
                        }
                        // Caso borde: si el valor ya viene como número string "2"
                        if (String.valueOf(op.getOrden()).equals(val)) {
                             return Double.valueOf(op.getOrden());
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            return null; // No se pudo convertir
        }
        return null; // Por defecto null si no es numérico
    }

    private int evaluarCriterio(CrfResumenRowDTO fila, CriterioFrontDTO criterio, List<CampoCrfStatsDTO> allStats) {
        if ("simple".equals(criterio.getTipo())) {
            // Lógica simple (Media, Mediana, etc) - Simplificado para este ejemplo
            // Se debería implementar buscando el valor y comparando con criterio.puntoCorte
            return 0; 
        } 
        else if ("complejo".equals(criterio.getTipo()) && criterio.getBloques() != null) {
            
            boolean globalResult = "AND".equals(criterio.getGlobalLogic()); // Inicial para AND es true, para OR es false? 
            // Mejor enfoque: coleccionar resultados de bloques
            List<Boolean> blockResults = new ArrayList<>();

            for (BloqueDTO bloque : criterio.getBloques()) {
                boolean blockResult = "AND".equals(bloque.getLogic()); // Base para AND

                List<Boolean> rulesResults = new ArrayList<>();
                for (ReglaDTO regla : bloque.getRules()) {
                    // Buscar el CampoCrf correspondiente al ID de la regla
                    CampoCrfStatsDTO campoStat = allStats.stream()
                        .filter(s -> String.valueOf(s.getCampoCrf().getIdCampo()).equals(regla.getCampoId()))
                        .findFirst().orElse(null);

                    if (campoStat != null) {
                        String valTexto = fila.getValores().get(campoStat.getColumnaKey());
                        Double valNum = obtenerValorNumerico(valTexto, campoStat.getCampoCrf());
                        rulesResults.add(evaluarRegla(valNum, valTexto, regla.getOperador(), regla.getValor()));
                    } else {
                        rulesResults.add(false);
                    }
                }

                if ("AND".equals(bloque.getLogic())) {
                    blockResult = rulesResults.stream().allMatch(b -> b);
                } else { // OR
                    blockResult = rulesResults.stream().anyMatch(b -> b);
                }
                blockResults.add(blockResult);
            }

            if ("AND".equals(criterio.getGlobalLogic())) {
                return blockResults.stream().allMatch(b -> b) ? 1 : 0;
            } else { // OR Global
                return blockResults.stream().anyMatch(b -> b) ? 1 : 0;
            }
        }
        return 0;
    }

    private boolean evaluarRegla(Double valNum, String valTexto, String op, String target) {
        if (valNum == null && (valTexto == null || "contains".equals(op))) return false;

        Double targetNum = null;
        try { targetNum = Double.parseDouble(target); } catch(Exception e){}

        if (targetNum != null && valNum != null) {
            // Comparación Numérica
            switch (op) {
                case "==": return valNum.equals(targetNum);
                case "!=": return !valNum.equals(targetNum);
                case ">": return valNum > targetNum;
                case "<": return valNum < targetNum;
                case ">=": return valNum >= targetNum;
                case "<=": return valNum <= targetNum;
            }
        } else if (valTexto != null) {
            // Comparación Texto
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

    // --- CLASES DTO INTERNAS PARA MAPEAR EL JSON DEL FRONTEND ---
    // (Deben coincidir con la estructura JS: criterio[uuid] = { ... })

    private Map<String, CriterioFrontDTO> parsearCriterios(String json) {
        try {
            if(json == null || json.isEmpty()) return new HashMap<>();
            return objectMapper.readValue(json, new TypeReference<HashMap<String, CriterioFrontDTO>>(){});
        } catch(Exception e) { return new HashMap<>(); }
    }

    private Set<String> parsearExcluidas(String json) {
        try {
            if(json == null || json.isEmpty()) return new HashSet<>();
            return objectMapper.readValue(json, new TypeReference<HashSet<String>>(){});
        } catch(Exception e) { return new HashSet<>(); }
    }

    // DTOs estáticos auxiliares para mapeo JSON
    public static class CriterioFrontDTO {
        public String nombreColumna;
        public String tipo; // "simple" o "complejo"
        public String globalLogic;
        public List<BloqueDTO> bloques;
        public String puntoCorte; // Para simple
        public String operador;   // Para simple
        
        // Getters
        public String getNombreColumna() { return nombreColumna; }
        public String getTipo() { return tipo; }
        public String getGlobalLogic() { return globalLogic; }
        public List<BloqueDTO> getBloques() { return bloques; }
        public String getPuntoCorte() { return puntoCorte; }
        public String getOperador() { return operador; }
    }

    public static class BloqueDTO {
        public String logic; // "AND" o "OR"
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