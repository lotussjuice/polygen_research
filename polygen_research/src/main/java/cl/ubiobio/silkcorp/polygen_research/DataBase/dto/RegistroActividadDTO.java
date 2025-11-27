package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import java.time.LocalDateTime;

public class RegistroActividadDTO {
    private Integer idRegistro;
    private Integer crfId;
    private String usuarioNombre;
    private String tipoActividad;
    private LocalDateTime fechaActividad;

    // Constructor vacío
    public RegistroActividadDTO() {
    }

    // Constructor
    public RegistroActividadDTO(Integer idRegistro, String usuarioNombre, String tipoActividad, LocalDateTime fechaActividad) {
        this.idRegistro = idRegistro;
        this.usuarioNombre = usuarioNombre;
        this.tipoActividad = tipoActividad;
        this.fechaActividad = fechaActividad;
    }

    // Getters y Setters
    public Integer getIdRegistro() { return idRegistro; }
    public void setIdRegistro(Integer idRegistro) { this.idRegistro = idRegistro; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getTipoActividad() { return tipoActividad; }
    public void setTipoActividad(String tipoActividad) { this.tipoActividad = tipoActividad; }

    public LocalDateTime getFechaActividad() { return fechaActividad; }
    public void setFechaActividad(LocalDateTime fechaActividad) { this.fechaActividad = fechaActividad; }

    public Integer getCrfId() { return crfId; }
    public void setCrfId(Integer crfId) { this.crfId = crfId; }
}