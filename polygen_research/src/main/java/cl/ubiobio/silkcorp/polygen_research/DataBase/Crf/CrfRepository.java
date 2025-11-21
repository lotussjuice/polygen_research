package cl.ubiobio.silkcorp.polygen_research.DataBase.Crf;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CrfRepository extends JpaRepository<Crf, Integer> {

    Optional<Crf> findByDatosPacienteCodigoPacienteIgnoreCase(String codigo);

    // Cuenta cuántos CRFs tienen el campo 'esCasoEstudio' en true
    long countByEsCasoEstudioTrue();
    
    // Lo contrario
    long countByEsCasoEstudioFalse();

    //
    Optional<Crf> findByEstadoAndDatosPacienteCodigoPacienteIgnoreCase(String estado, String codigo);
    
    // 1. Buscar por código (Cualquier estado)
    List<Crf> findByDatosPacienteCodigoPacienteContainingIgnoreCase(String codigo);

    // 2. Buscar por código Y estado (Solo Activos)
    List<Crf> findByEstadoAndDatosPacienteCodigoPacienteContainingIgnoreCase(String estado, String codigo);
    
    // 3. Buscar solo por estado (Todos los Activos)
    List<Crf> findByEstado(String estado);
}