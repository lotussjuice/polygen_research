package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import java.util.List;

public class CrfResumenViewDTO {

    private List<CampoCrfStatsDTO> camposConStats; 
    
    private List<CrfResumenRowDTO> filas; 

    public List<CampoCrfStatsDTO> getCamposConStats() {
        return camposConStats;
    }

    public void setCamposConStats(List<CampoCrfStatsDTO> camposConStats) {
        this.camposConStats = camposConStats;
    }

    @Deprecated
    public List<CampoCrfStatsDTO> getCamposActivos() {
        return camposConStats;
    }

    
    public List<CrfResumenRowDTO> getFilas() {
        return filas;
    }

    
    public void setFilas(List<CrfResumenRowDTO> filas) {
        this.filas = filas;
    }
}