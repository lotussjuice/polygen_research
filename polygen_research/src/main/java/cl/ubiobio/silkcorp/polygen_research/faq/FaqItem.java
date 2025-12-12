package cl.ubiobio.silkcorp.polygen_research.faq;

public class FaqItem {
    @SuppressWarnings("FieldMayBeFinal")
    private String pregunta;
    @SuppressWarnings("FieldMayBeFinal")
    private String respuesta;


    public FaqItem(String pregunta, String respuesta) {
        this.pregunta = pregunta;
        this.respuesta = respuesta;
    }

    public String getPregunta() { return pregunta; }
    public String getRespuesta() { return respuesta; }
}