package cl.ubiobio.silkcorp.polygen_research.DatosCrf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatosCrfRepository extends JpaRepository<DatosCrf, Integer> {
    
    // Aquí podrías buscar todos los datos de un CRF específico
    // List<DatosCrf> findByCrf_IdCrf(Integer crfId);
}