package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CriterioDTO {

    private String tipo;
    private String nombre;
    private String puntoCorte;
    private String campoNombre;
    
    private String nombreColumna;
    private String uuid;
    
    // --- CAMPO NUEVO AGREGADO ---
    private String operador; 
    // ----------------------------

    private List<ReglaDTO> reglas;

    // Getters y Setters

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

    public String getNombreColumna() {
        return nombreColumna;
    }

    public void setNombreColumna(String nombreColumna) {
        this.nombreColumna = nombreColumna;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    // --- GETTER Y SETTER NUEVOS ---
    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }
    // ------------------------------

    public List<ReglaDTO> getReglas() {
        return reglas;
    }

    public void setReglas(List<ReglaDTO> reglas) {
        this.reglas = reglas;
    }

    public static class ReglaDTO {
        private Integer campoId;
        private String operador;
        private String valor;

        public Integer getCampoId() {
            return campoId;
        }

        public void setCampoId(Integer campoId) {
            this.campoId = campoId;
        }

        public String getOperador() {
            return operador;
        }

        public void setOperador(String operador) {
            this.operador = operador;
        }

        public String getValor() {
            return valor;
        }

        public void setValor(String valor) {
            this.valor = valor;
        }
    }
}