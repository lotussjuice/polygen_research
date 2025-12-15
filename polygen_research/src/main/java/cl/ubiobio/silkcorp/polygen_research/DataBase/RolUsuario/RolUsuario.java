package cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;

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

    
    @OneToMany(mappedBy = "rolUsuario")
    private List<Usuario> usuarios;

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
    public List<Usuario> getUsuarios() { return usuarios; }
    public void setUsuarios(List<Usuario> usuarios) { this.usuarios = usuarios; }
}