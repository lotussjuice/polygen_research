package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente;

import java.util.List;
import java.util.Optional; // 1. IMPORTAR

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatosPacienteService {

    private final DatosPacienteRepository pacienteRepository;

    public DatosPacienteService(DatosPacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }
    
    // (Tu método getAllPacientes() con la validación de advertencias va aquí...)
    public List<DatosPaciente> getAllPacientes() {
        // ... (Tu lógica de validación de advertencias)
        return pacienteRepository.findAll(); // Asegúrate que esta lógica esté aquí
    }

    // --- ¡CAMBIOS AQUÍ! ---
    
    // 2. AÑADE @Transactional
    @Transactional(readOnly = true) 
    public Optional<DatosPaciente> getPacienteById(Integer id) {
        Optional<DatosPaciente> optPaciente = pacienteRepository.findById(id);
        
        if (optPaciente.isPresent()) {
            DatosPaciente paciente = optPaciente.get();
            
            // 3. "TOCAMOS" LAS LISTAS PARA FORZAR SU CARGA
            if (paciente.getCrfs() != null) {
                // Toca la lista de CRFs
                paciente.getCrfs().size(); 
                
                // Toca los datos dentro de cada CRF
                for (cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.Crf crf : paciente.getCrfs()) {
                    if (crf.getDatosCrfList() != null) {
                        crf.getDatosCrfList().size();
                    }
                }
            }
        }
        
        return optPaciente;
    }
    // --- FIN DE LOS CAMBIOS ---

    public DatosPaciente savePaciente(DatosPaciente paciente) {
        return pacienteRepository.save(paciente);
    }

    public void deletePaciente(Integer id) {
        pacienteRepository.deleteById(id);
    }
}