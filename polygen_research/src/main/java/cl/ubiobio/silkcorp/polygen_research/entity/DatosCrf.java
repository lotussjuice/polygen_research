package cl.ubiobio.silkcorp.polygen_research.entity;

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
}