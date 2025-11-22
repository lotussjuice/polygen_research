package cl.ubiobio.silkcorp.polygen_research.faq;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class FaqService {

    public List<FaqItem> obtenerPreguntas() {
        List<FaqItem> lista = new ArrayList<>();

        // ============================================================
        // ZONA EPICA DE EDICIÓN DE PREGUNTAS FRECUENTES
        // ============================================================

        lista.add(new FaqItem(
            "¿Cómo ingreso un nuevo paciente al sistema?", 
            "Para ingresar un paciente, dirígete al menú lateral 'Gestionar Pacientes' y haz clic en el botón 'Nuevo'."
        ));

        lista.add(new FaqItem(
            "¿Qué hago si me equivoqué en un registro CRF?", 
            "Si el estudio está abierto, puedes editar el registro desde 'Gestionar CRF'. Si no, contacta al Admin."
        ));

        lista.add(new FaqItem(
            "¿Cómo puedo cambiar mi contraseña?", 
            "No me lo vas a creer, pero si se te ocurre, POR ALGUN MOTIVO, hacer clic en el boton de Olvidé mi contraseña, que de forma casual resulta que es tu caso, entonces podrías tener la oportuinidad de recuperar tu tan preciada clave de acceso a este precioso sistema."
        ));
        
        // SE PUEDEN AGREGAR MAS PREGUNTAS SEIGUIENDO EL FORMATO
        
        return lista;
    }
}