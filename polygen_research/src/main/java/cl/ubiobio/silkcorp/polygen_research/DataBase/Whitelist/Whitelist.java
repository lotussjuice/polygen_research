package cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;
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

    public Integer getIdLista() {
        return idLista;
    }

    public void setIdLista(Integer idLista) {
        this.idLista = idLista;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}