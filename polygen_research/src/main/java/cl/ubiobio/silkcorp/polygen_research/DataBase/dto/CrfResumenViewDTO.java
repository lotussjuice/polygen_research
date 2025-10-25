package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import java.util.List;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;


public class CrfResumenViewDTO {

    // 1. La lista de columnas para el <thead>
    private List<CampoCrf> camposActivos; 
    
    // 2. La lista de filas para el <tbody>
    private List<CrfResumenRowDTO> filas; 

    
    // --- Getters y Setters Manuales ---

    // Getter para camposActivos
    public List<CampoCrf> getCamposActivos() {
        return camposActivos;
    }

    // Setter para camposActivos
    public void setCamposActivos(List<CampoCrf> camposActivos) {
        this.camposActivos = camposActivos;
    }

    // Getter para filas
    public List<CrfResumenRowDTO> getFilas() {
        return filas;
    }

    // Setter para filas
    public void setFilas(List<CrfResumenRowDTO> filas) {
        this.filas = filas;
    }
}