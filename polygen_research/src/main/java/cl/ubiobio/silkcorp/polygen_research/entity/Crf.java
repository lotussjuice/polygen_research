package cl.ubiobio.silkcorp.polygen_research.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate; // Usamos LocalDate para fechas, es más moderno
import java.util.List;

@Entity
@Table(name = "crf")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Crf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CRF")
    private Integer idCrf;

    @Column(name = "Grupo", length = 30)
    private String grupo;

    @Column(name = "Fecha_consulta")
    private LocalDate fechaConsulta; // Mapea a 'date' en MySQL

    @Column(name = "Estado", length = 20)
    private String estado;

    @Column(name = "Observacion", length = 200)
    private String observacion;
    
    // MUCHOS Crfs pertenecen a UN Paciente.
    @ManyToOne(fetch = FetchType.LAZY) // LAZY es bueno para performance
    @JoinColumn(name = "Datos_Paciente_ID", nullable = false)
    private DatosPaciente datosPaciente;

    // Un CRF tiene MUCHOS datos_crf
    @OneToMany(mappedBy = "crf")
    private List<DatosCrf> datosCrfList;

    // Un CRF tiene MUCHOS registros_actividad
    @OneToMany(mappedBy = "crf")
    private List<RegistroActividad> registrosActividad;
    
}