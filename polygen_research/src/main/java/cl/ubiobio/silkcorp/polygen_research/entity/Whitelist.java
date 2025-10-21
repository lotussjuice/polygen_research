package cl.ubiobio.silkcorp.polygen_research.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "whitelist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Whitelist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Lista")
    private Integer idLista;

    @Column(name = "Correo", length = 150, unique = true)
    private String correo;

    @Column(name = "Contrasena", length = 100)
    private String contrasena; // Recuerda encriptar esto en el servicio

    // --- Relación Uno-a-Uno ---
    // Esta entidad (Whitelist) "posee" la clave foránea.
    @OneToOne
    @JoinColumn(name = "Usuario_ID", referencedColumnName = "ID_Usuario", unique = true)
    private Usuario usuario;
}