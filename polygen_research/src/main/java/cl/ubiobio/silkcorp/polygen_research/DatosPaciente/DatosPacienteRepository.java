package cl.ubiobio.silkcorp.polygen_research.DatosPaciente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatosPacienteRepository extends JpaRepository<DatosPaciente, Integer> {
    // save(), findById(), findAll(), deleteById(), etc.
    
    // Podemos agregar métodos personalizados si es necesario, ej:
    // Optional<DatosPaciente> findByCodigoPaciente(String codigo);
}