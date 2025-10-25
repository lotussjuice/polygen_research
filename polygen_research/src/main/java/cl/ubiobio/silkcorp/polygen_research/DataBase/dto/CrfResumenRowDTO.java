package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import java.util.Map;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.Crf;



public class CrfResumenRowDTO {
    // Los datos fijos (Paciente, Fecha, etc.)
    private Crf crf; 
    
    // Los datos dinámicos: Map<ID_del_Campo, Valor_del_Campo>
    private Map<Integer, String> valores;

    // --- Getters y Setters Manuales ---

    public Crf getCrf() {
        return crf;
    }

    public void setCrf(Crf crf) {
        this.crf = crf;
    }

    // Método actualizado para devolver Map<Integer, String>
    public Map<Integer, String> getValores() {
        return valores;
    }

    // Método actualizado para aceptar Map<Integer, String>
    public void setValores(Map<Integer, String> valores) {
        this.valores = valores;
    } 
}