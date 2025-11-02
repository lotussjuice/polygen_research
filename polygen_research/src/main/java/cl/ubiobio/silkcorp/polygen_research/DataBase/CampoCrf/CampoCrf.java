package cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf.DatosCrf;

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

    @Column(name = "Activo") 
    private Boolean activo;

    @OneToMany(mappedBy = "campoCrf", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatosCrf> datosCrfList;

    public Integer getIdCampo() {
        return idCampo;
    }

    public void setIdCampo(Integer idCampo) {
        this.idCampo = idCampo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<DatosCrf> getDatosCrfList() {
        return datosCrfList;
    }

    public void setDatosCrfList(List<DatosCrf> datosCrfList) {
        this.datosCrfList = datosCrfList;
    }
}