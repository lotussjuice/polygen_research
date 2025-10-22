package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.Crf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "datos_crf")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatosCrf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_detalle")
    private Integer idDetalle;

    @Column(name = "Valor", length = 30)
    private String valor;

    // --- Clave Foránea (Relación N-a-1) a Crf ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CRF_ID", nullable = false)
    private Crf crf;

    // --- Clave Foránea (Relación N-a-1) a CampoCrf ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Campo_CRF_ID", nullable = false)
    private CampoCrf campoCrf;

    public Integer getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Crf getCrf() {
        return crf;
    }

    public void setCrf(Crf crf) {
        this.crf = crf;
    }

    public CampoCrf getCampoCrf() {
        return campoCrf;
    }

    public void setCampoCrf(CampoCrf campoCrf) {
        this.campoCrf = campoCrf;
    }
}