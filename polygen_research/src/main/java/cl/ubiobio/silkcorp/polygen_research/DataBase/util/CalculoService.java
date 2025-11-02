package cl.ubiobio.silkcorp.polygen_research.DataBase.util;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class CalculoService {

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


    public double calcularMediana(List<Double> valores) {
        if (valores == null || valores.isEmpty()) {
            return 0.0;
        }
        
        List<Double> sortedList = valores.stream().sorted().collect(Collectors.toList());
        
        int size = sortedList.size();
        if (size % 2 == 1) {
            return sortedList.get(size / 2);
        } else {
            double mid1 = sortedList.get((size / 2) - 1);
            double mid2 = sortedList.get(size / 2);
            return (mid1 + mid2) / 2.0;
        }
    }
}