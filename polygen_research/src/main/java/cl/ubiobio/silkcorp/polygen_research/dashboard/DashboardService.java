package cl.ubiobio.silkcorp.polygen_research.dashboard;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad.RegistroActividad;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad.RegistroActividadRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DashboardService {

    private final CrfRepository crfRepository;
    private final RegistroActividadRepository registroActividadRepository;
    
    // Formateador para la fecha
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

    public DashboardService(CrfRepository crfRepository, 
                            RegistroActividadRepository registroActividadRepository) {
        this.crfRepository = crfRepository;
        this.registroActividadRepository = registroActividadRepository;
    }

    // Establecer estadisticas para los mapas / gráficos
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Logica del gráfico circular / pastel
        long casosEstudio = crfRepository.countByEsCasoEstudioTrue();
        long casosConsulta = crfRepository.countByEsCasoEstudioFalse();
        
        stats.put("casosEstudio", casosEstudio);
        stats.put("casosConsulta", casosConsulta);

        long totalCrfs = casosEstudio + casosConsulta;
        stats.put("totalCrfs", totalCrfs);

        Optional<RegistroActividad> ultimoRegistro = registroActividadRepository.findTopByOrderByFechaActividadDesc();
        
        String fechaUltimoCambio = "N/A"; 
        if (ultimoRegistro.isPresent()) {
            LocalDateTime fecha = ultimoRegistro.get().getFechaActividad();
            fechaUltimoCambio = fecha.format(formatter);
        }
        stats.put("fechaUltimoCambio", fechaUltimoCambio);

        return stats;
    }
}