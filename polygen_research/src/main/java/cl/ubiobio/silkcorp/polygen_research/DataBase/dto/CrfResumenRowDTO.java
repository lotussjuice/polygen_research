package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import java.util.Map;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.Crf;



public class CrfResumenRowDTO {
    // Los datos fijos:
    private Crf crf; 
    // Los datos dinámicos:
    private Map<Integer, String> valores;


    public Crf getCrf() {
        return crf;
    }

    public void setCrf(Crf crf) {
        this.crf = crf;
    }

    public Map<Integer, String> getValores() {
        return valores;
    }

    public void setValores(Map<Integer, String> valores) {
        this.valores = valores;
    }
}