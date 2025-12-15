package cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

import cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad.RegistroActividad;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;

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

   
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Rol_Usuario_ID", nullable = false)
    @ToString.Exclude // placeholder, quitar si no cuenta con implementación 
    private RolUsuario rolUsuario;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Whitelist whitelist;

    @OneToMany(mappedBy = "usuario")
    private List<RegistroActividad> registrosActividad;

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public RolUsuario getRolUsuario() {
        return rolUsuario;
    }

    public void setRolUsuario(RolUsuario rolUsuario) {
        this.rolUsuario = rolUsuario;
    }

    public Whitelist getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(Whitelist whitelist) {
        this.whitelist = whitelist;
    }

    public List<RegistroActividad> getRegistrosActividad() {
        return registrosActividad;
    }

    public void setRegistrosActividad(List<RegistroActividad> registrosActividad) {
        this.registrosActividad = registrosActividad;
    }
}