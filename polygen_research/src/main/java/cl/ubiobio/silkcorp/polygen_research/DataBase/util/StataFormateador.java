package cl.ubiobio.silkcorp.polygen_research.DataBase.util;

import org.apache.poi.ss.usermodel.Cell;
import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StataFormateador {

    private static final Pattern PATTERN_CARACTERES_PROHIBIDOS = Pattern.compile("[^a-zA-Z0-9_]");

    private static final Pattern PATTERN_INICIO_NUMERO = Pattern.compile("^[0-9]");
   
    private static final Pattern PATTERN_TEXTO_NO_STATA = Pattern.compile("[\r\n\"]");

    /**
     * @param nombreOriginal
     * @return 
     */
    public static String formatarNombreVariable(String nombreOriginal) {
        if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) {
            return "var_empty";
        }
        
        String nombre = nombreOriginal.trim();

        nombre = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                           .replaceAll("[^\\p{ASCII}]", ""); 
        
        
        nombre = nombre.replaceAll("\\s", "_");
        Matcher matcher = PATTERN_CARACTERES_PROHIBIDOS.matcher(nombre);
        nombre = matcher.replaceAll("_");

        nombre = nombre.replaceAll("_{2,}", "_");
        nombre = nombre.replaceAll("^_|_$", "");
        
        if (PATTERN_INICIO_NUMERO.matcher(nombre).find() || nombre.isEmpty()) {
            nombre = "v_" + nombre;
        }
        
        nombre = nombre.toLowerCase();

    
        if (nombre.length() > 32) {
            nombre = nombre.substring(0, 32);
        }
        
        return nombre;
    }

    /**
     * @param valor
     * @return 
     */
    public static String formatarValor(String valor) {
        if (valor == null) {
            return "";
        }
        String valorLimpio = valor.trim();
        
        valorLimpio = PATTERN_TEXTO_NO_STATA.matcher(valorLimpio).replaceAll("");
        

        if (valorLimpio.matches("-?\\d+,\\d+.*")) {
            valorLimpio = valorLimpio.replace(',', '.');
        }
        
        return valorLimpio;
    }

    /**
     * @param cell 
     * @param valorFormateado
     */
    public static void escribirValorEnCelda(Cell cell, String valorFormateado) {
        if (valorFormateado == null || valorFormateado.isEmpty()) {
            cell.setCellValue("");
            return;
        }
        
        try {
            double valorNum = Double.parseDouble(valorFormateado);
            cell.setCellValue(valorNum);
        } catch (NumberFormatException e) {
            cell.setCellValue(valorFormateado);
        }
    }
}