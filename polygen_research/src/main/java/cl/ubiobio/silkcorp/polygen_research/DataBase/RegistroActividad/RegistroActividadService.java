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
        // Lógica de negocio:
        // Ej: Asignar la 'Fecha_actividad' al momento de guardar
        // registro.setFechaActividad(LocalDate.now());
        return registroRepository.save(registro);
    }

    public List<RegistroActividad> getAllRegistros() {
        return registroRepository.findAll();
    }
    

    public void logCrfActivity(String tipoActividad, Crf crf) {
        try {
            // 2. Obtenemos el email del usuario logueado
            String usernameEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // 3. Buscamos en la Whitelist usando el email
            Whitelist whitelistEntry = whitelistRepository.findByCorreo(usernameEmail)
                .orElseThrow(() -> new RuntimeException("Email no encontrado en Whitelist: " + usernameEmail));

            // 4. Obtenemos el Usuario desde la Whitelist
            // (Asumo que tu entidad Whitelist tiene un método getUsuario())
            Usuario usuarioActual = whitelistEntry.getUsuario();
            
            if (usuarioActual == null) {
                throw new RuntimeException("La entrada de Whitelist no tiene un usuario asociado.");
            }

            // 5. Creamos y guardamos el log (esto es igual que antes)
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