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
    private List<CampoValorDTO> campos; // Lista de pares Campo-Valor

    // Getters y Setters
    public Integer getIdCrf() { return idCrf; }
    public void setIdCrf(Integer idCrf) { this.idCrf = idCrf; }
    public LocalDate getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(LocalDate fechaConsulta) { this.fechaConsulta = fechaConsulta; }
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
    public String getCodigoPaciente() { return codigoPaciente; }
    public void setCodigoPaciente(String codigoPaciente) { this.codigoPaciente = codigoPaciente; }
    public Boolean getEsCasoEstudio() { return esCasoEstudio; }
    public void setEsCasoEstudio(Boolean esCasoEstudio) { this.esCasoEstudio = esCasoEstudio; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public List<CampoValorDTO> getCampos() { return campos; }
    public void setCampos(List<CampoValorDTO> campos) { this.campos = campos; }

    // Clase interna para los campos
    public static class CampoValorDTO {
        private String nombreCampo;
        private String valor;

        public CampoValorDTO(String nombreCampo, String valor) {
            this.nombreCampo = nombreCampo;
            this.valor = valor;
        }
        // Getters
        public String getNombreCampo() { return nombreCampo; }
        public String getValor() { return valor; }
    }
}