package cl.ubiobio.silkcorp.polygen_research.DataBase.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import cl.ubiobio.silkcorp.polygen_research.DataBase.OpcionCampoCrf.OpcionCampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CampoCrfStatsDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenRowDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.StataPreviewDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.util.CalculoService;

// Importamos los DTOs estáticos de ExcelReporteService para compartir la estructura del JSON
import cl.ubiobio.silkcorp.polygen_research.DataBase.export.ExcelReporteService.CriterioFrontDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.export.ExcelReporteService.BloqueDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.export.ExcelReporteService.ReglaDTO;

@Service
public class StataExportService {

    private final CrfService crfService;
    private final CalculoService calculoService;
    private final ObjectMapper objectMapper;

    // Patrones para sanitizar nombres de variables (Reglas de Stata)
    private static final Pattern PATTERN_ACENTOS = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern PATTERN_CARACTERES_INVALIDOS = Pattern.compile("[^a-zA-Z0-9_]");
    private static final Pattern PATTERN_INICIO_NUMERO = Pattern.compile("^[0-9]");
    private static final Pattern PATTERN_SALTO_LINEA = Pattern.compile("[\r\n]+");

    public StataExportService(CrfService crfService, CalculoService calculoService) {
        this.crfService = crfService;
        this.calculoService = calculoService;
        this.objectMapper = new ObjectMapper();
    }

    // --- GENERACIÓN DEL ARCHIVO ---
    public ByteArrayInputStream generarReporteStata(String criteriosJson, String excludedColsJson) throws IOException {
        StataPreviewDTO data = prepararDatosStata(criteriosJson, excludedColsJson);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Datos_STATA");

            // 1. Header
            Row headerRow = sheet.createRow(0);
            List<String> headers = data.getHeadersStata();
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }

            // 2. Datos
            int rowIdx = 1;
            List<Map<String, String>> filas = data.getFilasStata();

            for (Map<String, String> fila : filas) {
                Row dataRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i);
                    String valor = fila.get(header);

                    Cell cell = dataRow.createCell(i);
                    
                    if (valor == null || valor.isEmpty()) {
                        cell.setBlank(); // Importante: Celda vacía real para Nulos
                    } else {
                        try {
                            // Intentar guardar como número real para que Stata lo reconozca como numeric
                            double valNum = Double.parseDouble(valor.replace(',', '.'));
                            cell.setCellValue(valNum);
                        } catch (NumberFormatException e) {
                            // Si es texto, limpiarlo
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
        return prepararDatosStata(criteriosJson, "[]");
    }

    // --- LÓGICA CENTRAL CON VALIDACIÓN ---
    private StataPreviewDTO prepararDatosStata(String criteriosJson, String excludedColsJson) throws IOException {
        CrfResumenViewDTO data = crfService.getCrfResumenView(true);
        List<CampoCrfStatsDTO> columnasOriginales = data.getCamposConStats();
        List<CrfResumenRowDTO> filas = data.getFilas();

        // 1. SET DE VALIDACIÓN
        Set<String> idsCamposActivos = columnasOriginales.stream()
                .map(c -> String.valueOf(c.getCampoCrf().getIdCampo()))
                .collect(Collectors.toSet());

        // 2. Parsear JSONs
        Map<String, CriterioFrontDTO> criteriosMap;
        try {
            if (criteriosJson != null && !criteriosJson.isEmpty() && !criteriosJson.equals("{}")) {
                criteriosMap = objectMapper.readValue(criteriosJson, new TypeReference<HashMap<String, CriterioFrontDTO>>(){});
            } else {
                criteriosMap = new HashMap<>();
            }
        } catch (IOException e) { criteriosMap = new HashMap<>(); }

        Set<String> columnasExcluidas;
        try {
            if (excludedColsJson == null || excludedColsJson.isEmpty()) columnasExcluidas = new HashSet<>();
            else columnasExcluidas = objectMapper.readValue(excludedColsJson, new TypeReference<HashSet<String>>(){});
        } catch(Exception e) { columnasExcluidas = new HashSet<>(); }

        // 3. FILTRAR CRITERIOS VÁLIDOS
        List<String> uuidsOrdenados = new ArrayList<>();
        for (String uuid : criteriosMap.keySet()) {
            CriterioFrontDTO crit = criteriosMap.get(uuid);
            // Usamos la misma lógica de validación
            if (esCriterioValido(crit, idsCamposActivos)) {
                uuidsOrdenados.add(uuid);
            }
        }

        // 4. Pre-Calcular Medias y Medianas (Solo válidos)
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

        // 5. Preparar Estructuras
        List<String> headersOriginal = new ArrayList<>(); 
        List<String> headersStata = new ArrayList<>();    
        List<Map<String, String>> filasStata = new ArrayList<>();

        // Headers Fijos
        headersStata.add("id_crf");
        headersStata.add("cod_paciente");

        // Headers Dinámicos (Columnas Originales)
        for (CampoCrfStatsDTO stat : columnasOriginales) {
            String idStr = String.valueOf(stat.getCampoCrf().getIdCampo());
            String tipo = stat.getCampoCrf().getTipo();

            if (!columnasExcluidas.contains(idStr) && !"TEXTO".equals(tipo) && !"FECHA".equals(tipo)) {
                String nombreReal = stat.getNombreColumna();
                headersOriginal.add(nombreReal);
                headersStata.add(sanitizarNombreVariable(nombreReal));
            }
        }

        // Headers Calculados (SOLO VÁLIDOS)
        for (String uuid : uuidsOrdenados) {
            CriterioFrontDTO criterio = criteriosMap.get(uuid);
            headersOriginal.add(criterio.getNombreColumna());
            headersStata.add(sanitizarNombreVariable(criterio.getNombreColumna())); 
        }

        // 6. Procesar Filas
        for (CrfResumenRowDTO fila : filas) {
            Map<String, String> filaStata = new LinkedHashMap<>();

            // Datos Fijos
            filaStata.put("id_crf", String.valueOf(fila.getCrf().getIdCrf()));
            String codPac = fila.getCrf().getDatosPaciente() != null ? fila.getCrf().getDatosPaciente().getCodigoPaciente() : "";
            filaStata.put("cod_paciente", limpiarTextoStata(codPac));

            // Datos Originales
            for (CampoCrfStatsDTO stat : columnasOriginales) {
                String idStr = String.valueOf(stat.getCampoCrf().getIdCampo());
                String tipo = stat.getCampoCrf().getTipo();

                if (!columnasExcluidas.contains(idStr) && !"TEXTO".equals(tipo) && !"FECHA".equals(tipo)) {
                    String colKey = stat.getColumnaKey();
                    String valorCrudo = fila.getValores().get(colKey);
                    
                    Double valNum = obtenerValorNumerico(valorCrudo, stat.getCampoCrf());
                    String valorFinal = (valNum != null && !Double.isNaN(valNum)) ? String.valueOf(valNum) : null;
                    
                    filaStata.put(sanitizarNombreVariable(stat.getNombreColumna()), valorFinal);
                }
            }

            // Datos Calculados
            for (String uuid : uuidsOrdenados) {
                CriterioFrontDTO criterio = criteriosMap.get(uuid);
                Double puntoCorte = puntosCorteCache.get(uuid);
                
                Integer resultado = evaluarCriterioStata(fila, criterio, columnasOriginales, puntoCorte);
                
                String valorDicoStr = (resultado != null) ? String.valueOf(resultado) : null;
                filaStata.put(sanitizarNombreVariable(criterio.getNombreColumna()), valorDicoStr);
            }

            filasStata.add(filaStata);
        }

        return new StataPreviewDTO(headersOriginal, headersStata, null, filasStata);
    }

    // --- NUEVO: MÉTODO DE VALIDACIÓN ---
    private boolean esCriterioValido(CriterioFrontDTO crit, Set<String> idsActivos) {
        if ("complejo".equals(crit.getTipo())) {
            if (crit.getBloques() == null) return false;
            for (BloqueDTO bloque : crit.getBloques()) {
                if (bloque.getRules() == null) continue;
                for (ReglaDTO regla : bloque.getRules()) {
                    if (!idsActivos.contains(regla.getCampoId())) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return idsActivos.contains(crit.getOrigenId());
        }
    }

    private Integer evaluarCriterioStata(CrfResumenRowDTO fila, CriterioFrontDTO criterio, List<CampoCrfStatsDTO> allStats, Double puntoCortePrecalc) {
        // 1. Lógica Simple
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
        
        // 2. Lógica Compleja
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

    private double parseDouble(String valor) {
        if (valor == null || valor.trim().isEmpty() || valor.equals("-")) return Double.NaN;
        try { return Double.parseDouble(valor.trim().replace(',', '.')); } catch (NumberFormatException e) { return Double.NaN; }
    }

    // --- LIMPIEZA PARA STATA ---
    private String sanitizarNombreVariable(String nombreOriginal) {
        if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) return "var_sin_nombre";
        
        String nombre = Normalizer.normalize(nombreOriginal.trim(), Normalizer.Form.NFD);
        nombre = PATTERN_ACENTOS.matcher(nombre).replaceAll(""); // Quitar tildes
        
        nombre = nombre.replaceAll("\\s+", "_"); // Espacios a _
        nombre = PATTERN_CARACTERES_INVALIDOS.matcher(nombre).replaceAll("_"); // Símbolos a _
        
        nombre = nombre.replaceAll("_{2,}", "_"); // Evitar __
        nombre = nombre.replaceAll("^_+|_+$", ""); // Quitar _ al inicio/fin
        
        if (PATTERN_INICIO_NUMERO.matcher(nombre).find() || nombre.isEmpty()) {
            nombre = "v_" + nombre; // Stata no permite empezar con número
        }
        
        nombre = nombre.toLowerCase(); // Todo minúsculas
        
        if (nombre.length() > 32) {
            nombre = nombre.substring(0, 32); // Límite 32 chars
        }
        
        return nombre;
    }

    private String limpiarTextoStata(String texto) {
        if (texto == null) return null;
        String limpio = PATTERN_SALTO_LINEA.matcher(texto).replaceAll(" ");
        limpio = limpio.replace("\"", "'");
        return limpio.trim();
    }
}