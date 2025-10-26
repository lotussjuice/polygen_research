package cl.ubiobio.silkcorp.polygen_research.DataBase.util;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class CalculoService {

    /**
     * Calcula la Media (Promedio) de una lista de números.
     */
    public double calcularMedia(List<Double> valores) {
        if (valores == null || valores.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (double val : valores) {
            sum += val;
        }
        return sum / valores.size();
    }

    /**
     * Calcula la Mediana de una lista de números.
     */
    public double calcularMediana(List<Double> valores) {
        if (valores == null || valores.isEmpty()) {
            return 0.0;
        }
        
        // Copiamos la lista para no modificar la original y la ordenamos
        List<Double> sortedList = valores.stream().sorted().collect(Collectors.toList());
        
        int size = sortedList.size();
        if (size % 2 == 1) {
            // Tamaño impar
            return sortedList.get(size / 2);
        } else {
            // Tamaño par
            double mid1 = sortedList.get((size / 2) - 1);
            double mid2 = sortedList.get(size / 2);
            return (mid1 + mid2) / 2.0;
        }
    }
}