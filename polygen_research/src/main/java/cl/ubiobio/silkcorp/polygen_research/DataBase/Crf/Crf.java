package cl.ubiobio.silkcorp.polygen_research.DataBase.Crf;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf.DatosCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente.DatosPaciente;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad.RegistroActividad;
import jakarta.persistence.CascadeType; // Usamos LocalDate para fechas, es más moderno
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "crf")
public class Crf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CRF")
    private Integer idCrf;

    @Column(name = "Grupo", length = 30)
    private boolean esCasoEstudio;

    @Column(name = "Fecha_consulta")
    private LocalDate fechaConsulta = LocalDate.now();

    @Column(name = "Estado", length = 20)
    private String estado;

    @Column(name = "Observacion", length = 200)
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY es bueno para performance
    @JoinColumn(name = "Datos_Paciente_ID", nullable = false)
    private DatosPaciente datosPaciente;

    @OneToMany(mappedBy = "crf", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatosCrf> datosCrfList = new ArrayList<>();;

    @OneToMany(mappedBy = "crf")
    private List<RegistroActividad> registrosActividad;

    public Integer getIdCrf() {
        return idCrf;
    }

    public void setIdCrf(Integer idCrf) {
        this.idCrf = idCrf;
    }

    public boolean isCasoEstudio() {
        return esCasoEstudio;
    }

    public void setCasoEstudio(boolean grupo) {
        this.esCasoEstudio = grupo;
    }

    public LocalDate getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(LocalDate fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public DatosPaciente getDatosPaciente() {
        return datosPaciente;
    }

    public void setDatosPaciente(DatosPaciente datosPaciente) {
        this.datosPaciente = datosPaciente;
    }

    public List<DatosCrf> getDatosCrfList() {
        return datosCrfList;
    }

    public void setDatosCrfList(List<DatosCrf> datosCrfList) {
        this.datosCrfList = datosCrfList;
    }

    public List<RegistroActividad> getRegistrosActividad() {
        return registrosActividad;
    }

    public void setRegistrosActividad(List<RegistroActividad> registrosActividad) {
        this.registrosActividad = registrosActividad;
    }

    public void addDato(DatosCrf dato) {
        // Agrega la respuesta a la lista de este CRF
        datosCrfList.add(dato);
        // Establece la relación inversa (le dice a la respuesta quién es su "padre"
        // Crf)
        dato.setCrf(this);
    }
}