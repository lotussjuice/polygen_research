
package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CampoCrfStatsDTO {
    private CampoCrf campoCrf;
    private long countVacios;
    private long countCeros;
    private long countUnos;

    public CampoCrfStatsDTO(CampoCrf campoCrf, long countVacios, long countCeros, long countUnos) {
        this.campoCrf = campoCrf;
        this.countVacios = countVacios;
        this.countCeros = countCeros;
        this.countUnos = countUnos;
    }

    public CampoCrf getCampoCrf() { return campoCrf; }
    public long getCountVacios() { return countVacios; }
    public long getCountCeros() { return countCeros; }
    public long getCountUnos() { return countUnos; }
}