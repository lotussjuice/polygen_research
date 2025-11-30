package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import java.util.Map;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.Crf;

public class CrfResumenRowDTO {

    private Crf crf; 
    

    private Map<String, String> valores;
    
    private int datosFaltantes = 0; 

    public Crf getCrf() {
        return crf;
    }

    public void setCrf(Crf crf) {
        this.crf = crf;
    }

    public Map<String, String> getValores() {
        return valores;
    }

    public void setValores(Map<String, String> valores) {
        this.valores = valores;
    }

    public int getDatosFaltantes() {
        return datosFaltantes;
    }

    public void setDatosFaltantes(int datosFaltantes) {
        this.datosFaltantes = datosFaltantes;
    }
}