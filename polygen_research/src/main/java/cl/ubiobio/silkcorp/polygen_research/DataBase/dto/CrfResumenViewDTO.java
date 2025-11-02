package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import java.util.List;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;


public class CrfResumenViewDTO {

    //lista de columnas
    private List<CampoCrf> camposActivos; 
    
    //lista de filas
    private List<CrfResumenRowDTO> filas; 

    
    public List<CampoCrf> getCamposActivos() {
        return camposActivos;
    }

    
    public void setCamposActivos(List<CampoCrf> camposActivos) {
        this.camposActivos = camposActivos;
    }

    
    public List<CrfResumenRowDTO> getFilas() {
        return filas;
    }

    
    public void setFilas(List<CrfResumenRowDTO> filas) {
        this.filas = filas;
    }
}