package cl.ubiobio.silkcorp.polygen_research.CampoCrf;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import cl.ubiobio.silkcorp.polygen_research.DatosCrf.DatosCrf;

@Entity
@Table(name = "campo_crf")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampoCrf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Campo")
    private Integer idCampo;

    @Column(name = "Nombre", length = 50)
    private String nombre;

    @Column(name = "Tipo", length = 50)
    private String tipo;

    @Column(name = "Descripcion", length = 100)
    private String descripcion;

    @Column(name = "Activo") // tinyint(1) se mapea bien a Boolean
    private Boolean activo;

    // --- Relación Inversa ---
    // Un Campo puede estar en MUCHOS registros de DatosCrf.
    @OneToMany(mappedBy = "campoCrf", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatosCrf> datosCrfList;
}