package cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf;

import java.util.ArrayList;
import java.util.List;

import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf.DatosCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.OpcionCampoCrf.OpcionCampoCrf; 
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @OrderBy("idDetalle ASC")
    private List<DatosCrf> datosCrfList;

    @OneToMany(
        mappedBy = "campoCrf", 
        cascade = CascadeType.ALL, 
        orphanRemoval = true, 
        fetch = FetchType.LAZY 
    )
    @OrderBy("orden ASC") 
    private List<OpcionCampoCrf> opciones = new ArrayList<>();


    @Column(name = "Seccion")
    private Integer seccion;

    // NUEVO: La pregunta real que verá el usuario
    @Column(name = "Pregunta_Formulario", length = 255) 
    private String pregunta;

    public Integer getSeccion() {
        return seccion;
    }

    public void setSeccion(Integer seccion) {
        this.seccion = seccion;
    }

    public String getPreguntaFormulario() {
        return pregunta;
    }

    public void setPreguntaFormulario(String preguntaFormulario) {
        this.pregunta = preguntaFormulario;
    }



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

    public List<OpcionCampoCrf> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionCampoCrf> opciones) {
        this.opciones = opciones;
    }

    public void addOpcion(OpcionCampoCrf opcion) {
        opciones.add(opcion);
        opcion.setCampoCrf(this);
    }

    public void removeOpcion(OpcionCampoCrf opcion) {
        opciones.remove(opcion);
        opcion.setCampoCrf(null);
    }
}