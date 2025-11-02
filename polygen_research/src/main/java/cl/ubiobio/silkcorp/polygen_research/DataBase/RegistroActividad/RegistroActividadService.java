package cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.Crf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.UsuarioRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistRepository;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class RegistroActividadService {

    @Autowired
    private final RegistroActividadRepository registroRepository;

    @Autowired
    private WhitelistRepository whitelistRepository;

    public RegistroActividadService(RegistroActividadRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    public RegistroActividad saveRegistro(RegistroActividad registro) {
        return registroRepository.save(registro);
    }

    public List<RegistroActividad> getAllRegistros() {
        return registroRepository.findAll();
    }
    

    public void logCrfActivity(String tipoActividad, Crf crf) {
        try {
            // Obtener el email del usuario logueado
            String usernameEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // Buscar en la Whitelist usando el email
            Whitelist whitelistEntry = whitelistRepository.findByCorreo(usernameEmail)
                .orElseThrow(() -> new RuntimeException("Email no encontrado en Whitelist: " + usernameEmail));

            // Obtener el Usuario desde la Whitelist
            Usuario usuarioActual = whitelistEntry.getUsuario();
            
            if (usuarioActual == null) {
                throw new RuntimeException("La entrada de Whitelist no tiene un usuario asociado.");
            }

            // Crear y guardar el log
            RegistroActividad registro = new RegistroActividad();
            registro.setFechaActividad(LocalDateTime.now());
            registro.setTipoActividad(tipoActividad);
            registro.setUsuario(usuarioActual);
            registro.setCrf(crf); 

            registroRepository.save(registro);

        } catch (Exception e) {
            System.err.println("Error al guardar log de actividad: " + e.getMessage());
        }
    }
}