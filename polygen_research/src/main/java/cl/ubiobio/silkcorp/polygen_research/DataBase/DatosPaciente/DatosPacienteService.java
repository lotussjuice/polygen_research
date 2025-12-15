package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.Crf;

@Service
public class DatosPacienteService {

    private final DatosPacienteRepository pacienteRepository;

    public DatosPacienteService(DatosPacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }
    
    public List<DatosPaciente> getAllPacientes() {
        // Debes crear este método en el repositorio (ver paso 2)
        return pacienteRepository.findAll(); 
    }

    @Transactional(readOnly = true) 
    public Optional<DatosPaciente> getPacienteById(Integer id) {
        Optional<DatosPaciente> optPaciente = pacienteRepository.findById(id);
        
        if (optPaciente.isPresent()) {
            DatosPaciente paciente = optPaciente.get();
            
            if (paciente.getCrfs() != null) {
                paciente.getCrfs().size(); 
                
                for (cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.Crf crf : paciente.getCrfs()) {
                    if (crf.getDatosCrfList() != null) {
                        crf.getDatosCrfList().size();
                    }
                }
            }
        }
        
        return optPaciente;
    }

    public DatosPaciente savePaciente(DatosPaciente paciente) {
        return pacienteRepository.save(paciente);
    }

    public void deletePaciente(Integer id) {
        pacienteRepository.deleteById(id);
    }

    @Transactional
    public void toggleEstado(Integer id) {
        DatosPaciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        
        // Determinar nuevo estado (Toggle)
        String nuevoEstado = "ACTIVO".equals(paciente.getEstado()) ? "INACTIVO" : "ACTIVO";
        
        // 1. Cambiar estado del Paciente
        paciente.setEstado(nuevoEstado);
        
        // 2. Propagar cambio a sus CRFs (Requisito crítico)
        if (paciente.getCrfs() != null) {
            for (Crf crf : paciente.getCrfs()) {
                crf.setEstado(nuevoEstado);
            }
        }
        
        pacienteRepository.save(paciente);
    }
}