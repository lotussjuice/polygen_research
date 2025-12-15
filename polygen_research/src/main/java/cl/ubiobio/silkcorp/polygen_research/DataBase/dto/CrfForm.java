package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import java.util.ArrayList;
import java.util.List;

import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf.DatosCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente.DatosPaciente;

//@Getter
//@Setter
public class CrfForm {

    private DatosPaciente datosPaciente;
    private List<DatosCrf> datosCrfList;

    private boolean esCasoEstudio;
    private String observacion;

    private Integer idCrf;

    public CrfForm() {
        this.datosPaciente = new DatosPaciente();
        this.datosCrfList = new ArrayList<>();
    }

    public DatosPaciente getDatosPaciente() {
        return datosPaciente;
    }

    public void setDatosPaciente(DatosPaciente datosPaciente) {
        this.datosPaciente = datosPaciente;
    }

    public List<DatosCrf> getDatosCrfList() {
        return datosCrfList;
    }

    public void setDatosCrfList(List<DatosCrf> datosCrfList) {
        this.datosCrfList = datosCrfList;
    }

    public boolean isEsCasoEstudio() {
        return esCasoEstudio;
    }

    public void setEsCasoEstudio(boolean esCasoEstudio) {
        this.esCasoEstudio = esCasoEstudio;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Integer getIdCrf() { 
        return idCrf; 
    }
    
    public void setIdCrf(Integer idCrf) { 
        this.idCrf = idCrf; 
    }

    
}