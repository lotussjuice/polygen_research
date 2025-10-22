package cl.ubiobio.silkcorp.polygen_research.RegistroActividad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

import cl.ubiobio.silkcorp.polygen_research.Crf.Crf;
import cl.ubiobio.silkcorp.polygen_research.Usuario.Usuario;

@Entity
@Table(name = "registro_actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Registro")
    private Integer idRegistro;

    @Column(name = "Fecha_actividad")
    private LocalDate fechaActividad;

    @Column(name = "Tipo_actividad", length = 50)
    private String tipoActividad;

    // --- Clave Foránea (Relación N-a-1) a Crf ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CRF_ID", nullable = false)
    private Crf crf;

    // --- Clave Foránea (Relación N-a-1) a Usuario ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Usuario_ID", nullable = false)
    private Usuario usuario;
}