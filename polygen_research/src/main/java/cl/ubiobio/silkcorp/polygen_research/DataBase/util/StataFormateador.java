package cl.ubiobio.silkcorp.polygen_research.DataBase.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StataFormateador {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");


    public static String formatarNombreVariable(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return "variable_vacia";
        }

        // 1. Quitar acentos y caracteres especiales (ñ -> n, á -> a)
        String str = Normalizer.normalize(nombre, Normalizer.Form.NFD);
        str = DIACRITICS_PATTERN.matcher(str).replaceAll("");

        // 2. Reemplazar espacios por guión bajo
        str = str.replaceAll("\\s+", "_");

        // 3. Eliminar todos los símbolos no alfanuméricos (excepto '_')
        str = str.replaceAll("[^a-zA-Z0-9_]", "");

        // 4. Asegurar que no comience con un número
        if (Character.isDigit(str.charAt(0))) {
            str = "v_" + str;
        }

        // 5. Truncar a 32 caracteres
        if (str.length() > 32) {
            str = str.substring(0, 32);
        }

        return str.toLowerCase();
    }

    public static String formatarValor(String valor) {
        if (valor == null) {
            return "";
        }

        // 1. Quitar saltos de línea
        String str = valor.replaceAll("[\\r\\n]+", " ");

        // 2. Manejar comillas dobles (las reemplazamos por comillas simples)
        str = str.replace('"', '\'');
        
        // 3. Quitar acentos y caracteres extraños (opcional pero recomendado)
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = DIACRITICS_PATTERN.matcher(str).replaceAll("");
        
        // 4. Eliminar otros caracteres que puedan corromper (ej: tabulaciones)
        str = str.replaceAll("\t", " ");

        return str.trim();
    }
}