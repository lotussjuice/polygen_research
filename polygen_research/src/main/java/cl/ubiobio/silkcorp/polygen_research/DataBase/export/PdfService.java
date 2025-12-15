package cl.ubiobio.silkcorp.polygen_research.DataBase.export;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf.DatosCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente.DatosPaciente;
import cl.ubiobio.silkcorp.polygen_research.DataBase.OpcionCampoCrf.OpcionCampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfForm;

@Service
public class PdfService {

    private final CrfService crfService;
    
    private static final Color COLOR_PRIMARIO = new Color(52, 152, 219);
    private static final Color COLOR_SECUNDARIO = new Color(44, 62, 80);
    private static final Color COLOR_FONDO_ZEBRA = new Color(248, 249, 250);

    public PdfService(CrfService crfService) {
        this.crfService = crfService;
    }

    public Map<String, Object> generarPdfCrf(Integer crfId) {
        
        CrfForm form = crfService.prepararCrfFormParaEditar(crfId);
        DatosPaciente paciente = form.getDatosPaciente(); 

        Document document = new Document(PageSize.A4, 36, 36, 50, 50); 
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Map<String, Object> resultado = new HashMap<>();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_SECUNDARIO);
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.GRAY);
            Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            
            Font fontSeccionHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
            Font fontPregunta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_SECUNDARIO);
            Font fontRespuesta = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

            Paragraph titulo = new Paragraph("Reporte de Formulario CRF", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            
            Paragraph fechaImp = new Paragraph("Generado el: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()), 
                    FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY));
            fechaImp.setAlignment(Element.ALIGN_CENTER);
            document.add(fechaImp);
            document.add(Chunk.NEWLINE);

            PdfPTable tablaInfo = new PdfPTable(4); 
            tablaInfo.setWidthPercentage(100);
            tablaInfo.setWidths(new float[]{15f, 35f, 15f, 35f});
            tablaInfo.setSpacingAfter(15f);

            addCellInfo(tablaInfo, "Paciente:", fontLabel);
            addCellInfo(tablaInfo, paciente.getNombre() + " " + paciente.getApellido(), fontValue);
            addCellInfo(tablaInfo, "Código:", fontLabel);
            addCellInfo(tablaInfo, paciente.getCodigoPaciente(), fontValue);
            
            addCellInfo(tablaInfo, "Contacto:", fontLabel);
            addCellInfo(tablaInfo, paciente.getNumero(), fontValue);
            addCellInfo(tablaInfo, "Tipo:", fontLabel);
            addCellInfo(tablaInfo, form.isEsCasoEstudio() ? "Caso Estudio" : "Control", fontValue);

            document.add(tablaInfo);
            
            if(form.getObservacion() != null && !form.getObservacion().isEmpty()) {
                Paragraph obs = new Paragraph("Obs: " + form.getObservacion(), fontValue);
                obs.setSpacingAfter(15f);
                document.add(obs);
            }

            document.add(Chunk.NEWLINE);

            List<DatosCrf> datosOrdenados = new ArrayList<>(form.getDatosCrfList());
            Collections.sort(datosOrdenados, Comparator
                .comparing((DatosCrf d) -> d.getCampoCrf().getSeccion())
                .thenComparing(d -> d.getCampoCrf().getIdCampo()) 
            );

            PdfPTable tablaDatos = new PdfPTable(2);
            tablaDatos.setWidthPercentage(100);
            tablaDatos.setWidths(new float[]{60f, 40f}); 
            tablaDatos.setHeaderRows(0); 

            int seccionActual = -1;
            boolean alternateColor = false;

            for (DatosCrf dato : datosOrdenados) {
                if (!dato.getCampoCrf().isActivo()) continue;

                int sec = dato.getCampoCrf().getSeccion();

                if (sec != seccionActual) {
                    if (seccionActual != -1) {
                        PdfPCell spacer = new PdfPCell();
                        spacer.setColspan(2);
                        spacer.setFixedHeight(10f);
                        spacer.setBorder(Rectangle.NO_BORDER);
                        tablaDatos.addCell(spacer);
                    }

                    seccionActual = sec;
                    alternateColor = false; 

                    PdfPCell cellHeader = new PdfPCell(new Phrase(getNombreSeccion(sec), fontSeccionHeader));
                    cellHeader.setColspan(2);
                    cellHeader.setBackgroundColor(COLOR_PRIMARIO); 
                    cellHeader.setPadding(8f);
                    cellHeader.setBorder(Rectangle.NO_BORDER);
                    tablaDatos.addCell(cellHeader);
                }

                String textoPregunta = dato.getCampoCrf().getPreguntaFormulario();
                if (textoPregunta == null || textoPregunta.trim().isEmpty()) {
                    textoPregunta = dato.getCampoCrf().getDescripcion();
                }
                if (textoPregunta == null || textoPregunta.trim().isEmpty()) {
                    textoPregunta = dato.getCampoCrf().getNombre();
                }

                String valorMostrar = traducirValor(dato);

                PdfPCell cellKey = new PdfPCell(new Phrase(textoPregunta, fontPregunta));
                cellKey.setPadding(6f);
                cellKey.setBorder(Rectangle.BOTTOM);
                cellKey.setBorderColor(Color.LIGHT_GRAY);
                
                PdfPCell cellVal = new PdfPCell(new Phrase(valorMostrar, fontRespuesta));
                cellVal.setPadding(6f);
                cellVal.setBorder(Rectangle.BOTTOM);
                cellVal.setBorderColor(Color.LIGHT_GRAY);

                if (alternateColor) {
                    cellKey.setBackgroundColor(COLOR_FONDO_ZEBRA);
                    cellVal.setBackgroundColor(COLOR_FONDO_ZEBRA);
                } else {
                    cellKey.setBackgroundColor(Color.WHITE);
                    cellVal.setBackgroundColor(Color.WHITE);
                }
                alternateColor = !alternateColor;

                tablaDatos.addCell(cellKey);
                tablaDatos.addCell(cellVal);
            }

            document.add(tablaDatos);
            document.close();

            String codigoLimpio = paciente.getCodigoPaciente();
            if (codigoLimpio != null && (codigoLimpio.startsWith("C") || codigoLimpio.startsWith("E"))) {
                codigoLimpio = codigoLimpio.substring(1);
            }
            String prefijo = form.isEsCasoEstudio() ? "E" : "C";
            String filename = "CRF_" + prefijo + codigoLimpio + ".pdf";

            resultado.put("pdfStream", new ByteArrayInputStream(out.toByteArray()));
            resultado.put("filename", filename);

        } catch (DocumentException e) {
            e.printStackTrace();
            resultado.put("pdfStream", new ByteArrayInputStream(new byte[0]));
            resultado.put("filename", "Error.pdf");
        }

        return resultado;
    }

    private void addCellInfo(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private String traducirValor(DatosCrf dato) {
        String valor = dato.getValor();
        String tipo = dato.getCampoCrf().getTipo();

        if (valor == null || valor.trim().isEmpty()) return "-";

        if ("SI/NO".equals(tipo)) {
            if ("1".equals(valor)) return "Sí";
            if ("0".equals(valor)) return "No";
        } 
        else if ("SELECCION_UNICA".equals(tipo) && dato.getCampoCrf().getOpciones() != null) {
            for (OpcionCampoCrf op : dato.getCampoCrf().getOpciones()) {
                if (String.valueOf(op.getOrden()).equals(valor) || 
                    String.valueOf(op.getIdOpcion()).equals(valor)) { 
                    return op.getEtiqueta();
                }
            }
        }
        return valor;
    }

    private String getNombreSeccion(int seccionId) {
        return switch (seccionId) {
            case 1 -> "1. Información General";
            case 2 -> "2. Datos Sociodemográficos";
            case 3 -> "3. Antecedentes Clínicos";
            case 4 -> "4. Antropometría";
            case 5 -> "5. Hábitos - Tabaquismo";
            case 6 -> "6. Hábitos - Alcohol";
            case 7 -> "7. Hábitos - Alimentación";
            case 8 -> "8. Exposiciones";
            case 9 -> "9. Exámenes / Otros";
            default -> "Sección " + seccionId;
        };
    }
}