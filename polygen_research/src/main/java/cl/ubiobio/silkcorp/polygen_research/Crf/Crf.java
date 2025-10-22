package cl.ubiobio.silkcorp.polygen_research.Crf;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate; // Usamos LocalDate para fechas, es más moderno
import java.util.List;

import cl.ubiobio.silkcorp.polygen_research.DatosPaciente.DatosPaciente;
import cl.ubiobio.silkcorp.polygen_research.DatosCrf.DatosCrf;
import cl.ubiobio.silkcorp.polygen_research.RegistroActividad.RegistroActividad;

@Entity
@Table(name = "crf")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Crf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CRF")
    private Integer idCrf;

    @Column(name = "Grupo", length = 30)
    private String grupo;

    @Column(name = "Fecha_consulta")
    private LocalDate fechaConsulta; // Mapea a 'date' en MySQL

    @Column(name = "Estado", length = 20)
    private String estado;

    @Column(name = "Observacion", length = 200)
    private String observacion;
    
    // MUCHOS Crfs pertenecen a UN Paciente.
    @ManyToOne(fetch = FetchType.LAZY) // LAZY es bueno para performance
    @JoinColumn(name = "Datos_Paciente_ID", nullable = false)
    private DatosPaciente datosPaciente;

    // Un CRF tiene MUCHOS datos_crf
    @OneToMany(mappedBy = "crf")
    private List<DatosCrf> datosCrfList;

    // Un CRF tiene MUCHOS registros_actividad
    @OneToMany(mappedBy = "crf")
    private List<RegistroActividad> registrosActividad;
    
    public Integer getIdCrf() {
        return idCrf;
    }

    public void setIdCrf(Integer idCrf) {
        this.idCrf = idCrf;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
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
}