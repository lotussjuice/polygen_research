package cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad;

import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class RegistroActividadService {

    private final RegistroActividadRepository registroRepository;

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
    
}