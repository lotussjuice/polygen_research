package cl.ubiobio.silkcorp.polygen_research.service;

import cl.ubiobio.silkcorp.polygen_research.entity.RegistroActividad;
import cl.ubiobio.silkcorp.polygen_research.repository.RegistroActividadRepository;
import org.springframework.stereotype.Service;

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
    
    // ... otros métodos
}