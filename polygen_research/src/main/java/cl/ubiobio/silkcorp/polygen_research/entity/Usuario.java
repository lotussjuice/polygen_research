package cl.ubiobio.silkcorp.polygen_research.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Usuario")
    private Integer idUsuario;

    @Column(name = "Nombre_Usuario", length = 100)
    private String nombreUsuario;

    @Column(name = "Estado", length = 20)
    private String estado;

    // --- Clave Foránea (Relación N-a-1) a RolUsuario ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Rol_Usuario_ID", nullable = false)
    private RolUsuario rolUsuario;

    // --- Relación Inversa (Uno-a-Uno) con Whitelist ---
    // "mappedBy" indica que la entidad "Whitelist" es la dueña de esta relación
    // y ya la definió en su campo "usuario".
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Whitelist whitelist;

    // --- Relación Inversa (Uno-a-Muchos) con RegistroActividad ---
    @OneToMany(mappedBy = "usuario")
    private List<RegistroActividad> registrosActividad;
}