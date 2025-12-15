package cl.ubiobio.silkcorp.polygen_research.faq;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class FaqService {

    public List<FaqItem> obtenerPreguntas() {
        List<FaqItem> lista = new ArrayList<>();

        // ZONA EPICA DE EDICIÓN DE PREGUNTAS FRECUENTES

        lista.add(new FaqItem(
            "¿Cómo contacto a soporte técnico?", 
            "Para reportar fallos o dudas críticas, escriba a <a href='https://mail.google.com/mail/?view=cm&fs=1&to=polygenresearchsys@gmail.com&su=Soporte%20Polygen%20Research' target='_blank'>polygenresearchsys@gmail.com</a>. Incluya detalles del error o capturas de pantalla para agilizar la ayuda. <br><br/>"
        ));

        lista.add(new FaqItem(
            "¿En qué formatos puedo descargar la información?", 
            "El sistema permite la exportación en tres formatos: PDF (vista individual), Excel (reportes generales) y archivos compatibles con STATA (.dta) para análisis estadístico avanzado. <br><br/>"
        ));

        lista.add(new FaqItem(
            "¿Quién tiene acceso a los datos sensibles?", 
            "El acceso está restringido según el rol (Investigador, Admin, etc.). El sistema cumple protocolos de confidencialidad y los datos exportados pueden ser anonimizados según la configuración del estudio. <br><br/>"
        ));

        lista.add(new FaqItem(
            "¿Qué criterio define a un paciente como Caso o Control?", 
            "Un paciente 'Caso' tiene diagnóstico confirmado de cáncer gástrico. Un paciente 'Control' está en seguimiento sin patología demostrada. Es vital seleccionar la categoría correcta al crear el CRF. <br><br/>"
        ));

        lista.add(new FaqItem(
            "¿Puedo eliminar un paciente registrado por error?", 
            "Por integridad de los datos, la eliminación directa no está permitida para usuarios estándar. Si duplicó un registro, contacte al administrador para solicitar la baja de este. <br><br/>"
        ));

        lista.add(new FaqItem(
            "¿Qué pasa si me equivoco al ingresar datos en un formulario?", 
            "Puede dirigirse al menú 'Gestionar CRF' o 'Pacientes', buscar el registro y seleccionar la opción de edición y hacer los cambios necesarios. <br><br/>"
        ));

        lista.add(new FaqItem(
            "¿Qué hago si olvidé mi contraseña?", 
            "En la pantalla de inicio, haga clic en '¿Olvidó su contraseña?'. El sistema le enviará un código de verificación único a su correo (similar al registro). Ingrese ese código para crear una nueva clave. <br><br/>"
        ));
        
        return lista;
    }
}