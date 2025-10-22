package cl.ubiobio.silkcorp.polygen_research.DatosPaciente;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

import cl.ubiobio.silkcorp.polygen_research.Crf.Crf;

@Entity
@Table(name = "datos_paciente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatosPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Paciente")
    private Integer idPaciente;

    @Column(name = "Codigo_Paciente", length = 50, unique = true)
    private String codigoPaciente; // Es buena idea que el código sea único

    @Column(name = "Nombre", length = 30)
    private String nombre;

    @Column(name = "Apellido", length = 30)
    private String apellido;

    @Column(name = "Numero", length = 20)
    private String numero;

    @Column(name = "Direccion", length = 100)
    private String direccion;

    @Column(name = "Estado", length = 20)
    private String estado;

    // --- Relación Inversa ---
    // Un paciente puede tener MUCHOS Crfs.
    // "mappedBy" le dice a JPA que la clave foránea está en la entidad Crf, 
    // en el campo "datosPaciente".
    @OneToMany(mappedBy = "datosPaciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Crf> crfs;

    public Integer getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Integer idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getCodigoPaciente() {
        return codigoPaciente;
    }

    public void setCodigoPaciente(String codigoPaciente) {
        this.codigoPaciente = codigoPaciente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<Crf> getCrfs() {
        return crfs;
    }

    public void setCrfs(List<Crf> crfs) {
        this.crfs = crfs;
    }
}