package cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.Crf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;

@Entity
@Table(name = "registro_actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Registro")
    private Integer idRegistro;

    @Column(name = "Fecha_actividad")
    private LocalDateTime fechaActividad;

    @Column(name = "Tipo_actividad", length = 50)
    private String tipoActividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CRF_ID", nullable = false)
    private Crf crf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Usuario_ID", nullable = false)
    private Usuario usuario;

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
    }

    public LocalDateTime getFechaActividad() {
        return fechaActividad;
    }

    public void setFechaActividad(LocalDateTime fechaActividad) {
        this.fechaActividad = fechaActividad;
    }

    public String getTipoActividad() {
        return tipoActividad;
    }

    public void setTipoActividad(String tipoActividad) {
        this.tipoActividad = tipoActividad;
    }

    public Crf getCrf() {
        return crf;
    }

    public void setCrf(Crf crf) {
        this.crf = crf;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}