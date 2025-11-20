package cl.ubiobio.silkcorp.polygen_research.DataBase.OpcionCampoCrf;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "opcion_campo_crf")
@Getter
@Setter

public class OpcionCampoCrf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Opcion")
    private Integer idOpcion;

    @Column(name = "Etiqueta", length = 100)
    private String etiqueta;

    @Column(name = "Orden")
    private Integer orden; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Campo")
    private CampoCrf campoCrf;

    public OpcionCampoCrf() {
    }

    public OpcionCampoCrf(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public Integer getIdOpcion() {
        return idOpcion;
    }

    public void setIdOpcion(Integer idOpcion) {
        this.idOpcion = idOpcion;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public CampoCrf getCampoCrf() {
        return campoCrf;
    }

    public void setCampoCrf(CampoCrf campoCrf) {
        this.campoCrf = campoCrf;
    }
}