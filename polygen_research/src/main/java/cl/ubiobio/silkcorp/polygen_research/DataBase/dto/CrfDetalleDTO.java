package cl.ubiobio.silkcorp.polygen_research.DataBase.dto;

import java.time.LocalDate;
import java.util.List;

public class CrfDetalleDTO {
    private Integer idCrf;
    private LocalDate fechaConsulta;
    private String nombrePaciente;
    private String codigoPaciente;
    private Boolean esCasoEstudio;
    private String observacion;
    private List<CampoValorDTO> campos;

    // Constructor vacío
    public CrfDetalleDTO() {
    }

    // Constructor con todos los campos
    public CrfDetalleDTO(Integer idCrf, LocalDate fechaConsulta, String nombrePaciente, String codigoPaciente, Boolean esCasoEstudio, String observacion, List<CampoValorDTO> campos) {
        this.idCrf = idCrf;
        this.fechaConsulta = fechaConsulta;
        this.nombrePaciente = nombrePaciente;
        this.codigoPaciente = codigoPaciente;
        this.esCasoEstudio = esCasoEstudio;
        this.observacion = observacion;
        this.campos = campos;
    }

    // Getters y Setters manuales
    public Integer getIdCrf() {
        return idCrf;
    }

    public void setIdCrf(Integer idCrf) {
        this.idCrf = idCrf;
    }

    public LocalDate getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(LocalDate fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    public String getCodigoPaciente() {
        return codigoPaciente;
    }

    public void setCodigoPaciente(String codigoPaciente) {
        this.codigoPaciente = codigoPaciente;
    }

    public Boolean getEsCasoEstudio() {
        return esCasoEstudio;
    }

    public void setEsCasoEstudio(Boolean esCasoEstudio) {
        this.esCasoEstudio = esCasoEstudio;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public List<CampoValorDTO> getCampos() {
        return campos;
    }

    public void setCampos(List<CampoValorDTO> campos) {
        this.campos = campos;
    }

    public static class CampoValorDTO {
        private String nombreCampo;
        private String valor;
        private Integer seccion; // Nuevo campo necesario para la agrupación

        public CampoValorDTO() {
        }

        public CampoValorDTO(String nombreCampo, String valor, Integer seccion) {
            this.nombreCampo = nombreCampo;
            this.valor = valor;
            this.seccion = seccion;
        }

        public String getNombreCampo() {
            return nombreCampo;
        }

        public void setNombreCampo(String nombreCampo) {
            this.nombreCampo = nombreCampo;
        }

        public String getValor() {
            return valor;
        }

        public void setValor(String valor) {
            this.valor = valor;
        }

        public Integer getSeccion() {
            return seccion;
        }

        public void setSeccion(Integer seccion) {
            this.seccion = seccion;
        }
    }
}