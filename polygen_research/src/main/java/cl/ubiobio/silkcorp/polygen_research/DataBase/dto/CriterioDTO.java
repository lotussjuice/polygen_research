package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CriterioDTO {

    private String tipo;
    private String nombre;
    private String puntoCorte;
    private String campoNombre; 

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPuntoCorte() {
        return puntoCorte;
    }

    public void setPuntoCorte(String puntoCorte) {
        this.puntoCorte = puntoCorte;
    }

    public String getCampoNombre() {
        return campoNombre;
    }

    public void setCampoNombre(String campoNombre) {
        this.campoNombre = campoNombre;
    }
}