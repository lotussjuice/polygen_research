package cl.ubiobio.silkcorp.polygen_research.DataBase.export;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf.DatosCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente.DatosPaciente;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfForm;

// --- CORRECCIÓN: Importación exacta de tu clase ---
import cl.ubiobio.silkcorp.polygen_research.DataBase.OpcionCampoCrf.OpcionCampoCrf;

@Service
public class PdfService {

    private final CrfService crfService; 

    public PdfService(CrfService crfService) {
        this.crfService = crfService;
    }

    public Map<String, Object> generarPdfCrf(Integer crfId) {
        
        CrfForm form = crfService.prepararCrfFormParaEditar(crfId);
        DatosPaciente paciente = form.getDatosPaciente(); 

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Map<String, Object> resultado = new HashMap<>();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fuentes
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLUE);
            Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font fontCelda = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fontCeldaBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            // Título
            Paragraph titulo = new Paragraph("Reporte de Formulario CRF", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(Chunk.NEWLINE);

            // Datos del Paciente
            document.add(new Paragraph("1. Datos del Paciente", fontSubtitulo));
            document.add(Chunk.NEWLINE);
            
            PdfPTable tablaPaciente = new PdfPTable(2);
            tablaPaciente.setWidthPercentage(100);
            addCeldaTabla(tablaPaciente, "Cód. Paciente:", fontCeldaBold);
            addCeldaTabla(tablaPaciente, paciente.getCodigoPaciente(), fontCelda);
            addCeldaTabla(tablaPaciente, "Nombre:", fontCeldaBold);
            addCeldaTabla(tablaPaciente, paciente.getNombre() + " " + paciente.getApellido(), fontCelda);
            addCeldaTabla(tablaPaciente, "Número Contacto:", fontCeldaBold);
            addCeldaTabla(tablaPaciente, paciente.getNumero(), fontCelda);
            addCeldaTabla(tablaPaciente, "Dirección:", fontCeldaBold);
            addCeldaTabla(tablaPaciente, paciente.getDireccion(), fontCelda);
            document.add(tablaPaciente);

            // Datos del Estudio
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("2. Datos del Estudio", fontSubtitulo));
            document.add(Chunk.NEWLINE);

            PdfPTable tablaEstudio = new PdfPTable(2);
            tablaEstudio.setWidthPercentage(100);
            
            addCeldaTabla(tablaEstudio, "Tipo de Estudio:", fontCeldaBold);
            addCeldaTabla(tablaEstudio, form.isEsCasoEstudio() ? "Caso Estudio" : "Caso Control", fontCelda);
            addCeldaTabla(tablaEstudio, "Observaciones:", fontCeldaBold);
            addCeldaTabla(tablaEstudio, form.getObservacion() != null ? form.getObservacion() : "N/A", fontCelda);
            document.add(tablaEstudio);
            
            // Campos Dinámicos
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("3. Campos del Formulario", fontSubtitulo));
            document.add(Chunk.NEWLINE);

            PdfPTable tablaDatos = new PdfPTable(2); 
            tablaDatos.setWidthPercentage(100);

            // Encabezado
            PdfPCell headerPregunta = new PdfPCell(new Phrase("Campo", fontHeaderTabla));
            headerPregunta.setBackgroundColor(Color.DARK_GRAY);
            tablaDatos.addCell(headerPregunta);
            
            PdfPCell headerRespuesta = new PdfPCell(new Phrase("Valor", fontHeaderTabla));
            headerRespuesta.setBackgroundColor(Color.DARK_GRAY);
            tablaDatos.addCell(headerRespuesta);

            // Filas de datos con TRADUCCIÓN
            for (DatosCrf dato : form.getDatosCrfList()) {
                addCeldaTabla(tablaDatos, dato.getCampoCrf().getNombre(), fontCeldaBold);

                String valor = dato.getValor();
                String tipo = dato.getCampoCrf().getTipo();
                String valorMostrar = valor;

                // Lógica de Traducción
                if (valor != null && !valor.trim().isEmpty()) {
                    if ("SI/NO".equals(tipo)) {
                        // Traducir 1/0 a Sí/No
                        if ("1".equals(valor)) valorMostrar = "Sí";
                        else if ("0".equals(valor)) valorMostrar = "No";
                    } 
                    else if ("SELECCION_UNICA".equals(tipo)) {
                        // Traducir ID de opción a Etiqueta (Texto)
                        if (dato.getCampoCrf().getOpciones() != null) {
                            // CORRECCIÓN: Usamos el tipo correcto OpcionCampoCrf
                            for (OpcionCampoCrf op : dato.getCampoCrf().getOpciones()) {
                                // Comparamos el valor guardado (String) con el orden de la opción (Integer)
                                if (String.valueOf(op.getOrden()).equals(valor)) {
                                    valorMostrar = op.getEtiqueta();
                                    break; // Encontrado, salimos del bucle
                                }
                            }
                        }
                    }
                } else {
                    valorMostrar = "-";
                }

                addCeldaTabla(tablaDatos, valorMostrar, fontCelda);
            }
            document.add(tablaDatos);

            document.close();

            // Nombre del archivo
            String codigoLimpio = paciente.getCodigoPaciente();
            if (codigoLimpio != null && (codigoLimpio.startsWith("C") || codigoLimpio.startsWith("E"))) {
                codigoLimpio = codigoLimpio.substring(1);
            }

            String prefijo = form.isEsCasoEstudio() ? "E" : "C";
            String filename = prefijo + codigoLimpio + ".pdf";

            resultado.put("pdfStream", new ByteArrayInputStream(out.toByteArray()));
            resultado.put("filename", filename); 

        } catch (DocumentException e) {
            e.printStackTrace();
            resultado.put("pdfStream", new ByteArrayInputStream(new byte[0])); 
            resultado.put("filename", "Error_CRF_" + crfId + ".pdf");
        }

        return resultado; 
    }

    private void addCeldaTabla(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }
}