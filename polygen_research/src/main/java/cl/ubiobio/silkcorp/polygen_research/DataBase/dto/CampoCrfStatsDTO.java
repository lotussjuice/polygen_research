package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.OpcionCampoCrf.OpcionCampoCrf;

public class CampoCrfStatsDTO {
    
    private CampoCrf campoCrf;
    private OpcionCampoCrf opcion; // Puede ser null si no es un desglose
    
    private String columnaKey;     // Clave única para el mapa de valores
    private String nombreColumna;  // Nombre a mostrar

    private long countVacios;
    private long countCeros;
    private long countUnos;

    // Constructor Normal
    public CampoCrfStatsDTO(CampoCrf campoCrf, String columnaKey, String nombreColumna, long countVacios, long countCeros, long countUnos) {
        this.campoCrf = campoCrf;
        this.columnaKey = columnaKey;
        this.nombreColumna = nombreColumna;
        this.countVacios = countVacios;
        this.countCeros = countCeros;
        this.countUnos = countUnos;
    }
    
    // Constructor con Opción (Para One-Hot)
    public CampoCrfStatsDTO(CampoCrf campoCrf, OpcionCampoCrf opcion, String columnaKey, String nombreColumna, long countVacios, long countCeros, long countUnos) {
        this(campoCrf, columnaKey, nombreColumna, countVacios, countCeros, countUnos);
        this.opcion = opcion;
    }

    // --- GETTERS Y SETTERS MANUALES ---

    public CampoCrf getCampoCrf() {
        return campoCrf;
    }

    public void setCampoCrf(CampoCrf campoCrf) {
        this.campoCrf = campoCrf;
    }

    public OpcionCampoCrf getOpcion() {
        return opcion;
    }

    public void setOpcion(OpcionCampoCrf opcion) {
        this.opcion = opcion;
    }

    public String getColumnaKey() {
        return columnaKey;
    }

    public void setColumnaKey(String columnaKey) {
        this.columnaKey = columnaKey;
    }

    public String getNombreColumna() {
        return nombreColumna;
    }

    public void setNombreColumna(String nombreColumna) {
        this.nombreColumna = nombreColumna;
    }

    public long getCountVacios() {
        return countVacios;
    }

    public void setCountVacios(long countVacios) {
        this.countVacios = countVacios;
    }

    public long getCountCeros() {
        return countCeros;
    }

    public void setCountCeros(long countCeros) {
        this.countCeros = countCeros;
    }

    public long getCountUnos() {
        return countUnos;
    }

    public void setCountUnos(long countUnos) {
        this.countUnos = countUnos;
    }
}