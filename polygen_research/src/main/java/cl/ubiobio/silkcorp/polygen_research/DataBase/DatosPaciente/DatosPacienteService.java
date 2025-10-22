package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DatosPacienteService {

    private final DatosPacienteRepository pacienteRepository;

    // Inyección de dependencias por constructor (recomendado)
    public DatosPacienteService(DatosPacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public List<DatosPaciente> getAllPacientes() {
        return pacienteRepository.findAll();
    }

    public Optional<DatosPaciente> getPacienteById(Integer id) {
        return pacienteRepository.findById(id);
    }

    public DatosPaciente savePaciente(DatosPaciente paciente) {
        // Aquí iría lógica de negocio, ej: validar que el código no exista
        return pacienteRepository.save(paciente);
    }

    public void deletePaciente(Integer id) {
        pacienteRepository.deleteById(id);
    }
}