package cl.ubiobio.silkcorp.polygen_research.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    
    public void enviarCodigoVerificacion(String destinatario, String nombreUsuario, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        
        message.setFrom(remitente);
        message.setTo(destinatario);
        message.setSubject("Verificación de Cuenta - Polygen Research");

        // Construcción del mensaje formal
        String cuerpoMensaje = """
            Estimado(a) %s,
            
            Gracias por registrarse en la plataforma Polygen Research (DataClinic).
            
            Para completar su proceso de registro y verificar la seguridad de su cuenta, 
            por favor ingrese el siguiente código de verificación en la pantalla del sistema:
            
            =========================
            CÓDIGO:  %s
            =========================
            
            Si usted no ha solicitado este registro, por favor ignore este mensaje o póngase 
            en contacto con el administrador del sistema.
            
            Atentamente,
            
            Equipo de Polygen Research
            Universidad del Bío-Bío
            """;

        // Formateamos el texto insertando el nombre y el código
        String textoFinal = String.format(cuerpoMensaje, nombreUsuario, codigo);

        message.setText(textoFinal);
        
        mailSender.send(message);
    }

    public void enviarCodigoRecuperacion(String destinatario, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(remitente);
        message.setTo(destinatario);
        message.setSubject("Restablecer Contraseña - Polygen Research");

        String cuerpoMensaje = """
            Estimado usuario,
            
            Hemos recibido una solicitud para restablecer la contraseña de su cuenta.
            
            Para continuar, ingrese el siguiente código de seguridad en la pantalla de recuperación:
            
            =========================
            CÓDIGO:  %s
            =========================
            
            Si usted no solicitó este cambio, por favor ignore este correo y su contraseña permanecerá segura.
            
            Atentamente,
            Equipo de Polygen Research
            """;

        message.setText(String.format(cuerpoMensaje, codigo));
        mailSender.send(message);
    }
}