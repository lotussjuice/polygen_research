package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import java.util.List;
import java.util.Map;

public class StataPreviewDTO {
    
    // Listas de nombres de columnas
    private List<String> headersOriginal;
    private List<String> headersStata;
    
    
    private List<Map<String, String>> filasOriginal;
    
    
    private List<Map<String, String>> filasStata;

    
    public StataPreviewDTO() {
    }

    
    public StataPreviewDTO(List<String> headersOriginal, List<String> headersStata, List<Map<String, String>> filasOriginal, List<Map<String, String>> filasStata) {
        this.headersOriginal = headersOriginal;
        this.headersStata = headersStata;
        this.filasOriginal = filasOriginal;
        this.filasStata = filasStata;
    }


    public List<String> getHeadersOriginal() {
        return headersOriginal;
    }

    public void setHeadersOriginal(List<String> headersOriginal) {
        this.headersOriginal = headersOriginal;
    }

    public List<String> getHeadersStata() {
        return headersStata;
    }

    public void setHeadersStata(List<String> headersStata) {
        this.headersStata = headersStata;
    }

    public List<Map<String, String>> getFilasOriginal() {
        return filasOriginal;
    }

    public void setFilasOriginal(List<Map<String, String>> filasOriginal) {
        this.filasOriginal = filasOriginal;
    }

    public List<Map<String, String>> getFilasStata() {
        return filasStata;
    }

    public void setFilasStata(List<Map<String, String>> filasStata) {
        this.filasStata = filasStata;
    }
}