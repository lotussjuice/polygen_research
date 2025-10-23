package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import java.util.ArrayList;
import java.util.List;

import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf.DatosCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente.DatosPaciente;
//import lombok.Getter;
//import lombok.Setter;

/**
 * Este DTO (Data Transfer Object) actúa como un "Wrapper" o "Contenedor"
 * para el formulario de Thymeleaf. Agrupa los datos del paciente (estáticos)
 * y la lista de respuestas (dinámicas).
 */
//@Getter
//@Setter
public class CrfForm {

    // 1. Objeto para los campos estáticos de DatosPaciente
    private DatosPaciente datosPaciente;

    // 2. Lista para los campos dinámicos (las respuestas)
    private List<DatosCrf> datosCrfList;

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
}