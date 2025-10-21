package cl.ubiobio.silkcorp.polygen_research.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "rol_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Rol")
    private Integer idRol;

    @Column(name = "Nombre_Rol", length = 20)
    private String nombreRol;

    // --- Relación Inversa ---
    // Un Rol puede tener MUCHOS Usuarios.
    @OneToMany(mappedBy = "rolUsuario")
    private List<Usuario> usuarios;
}