package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatosCrfRepository extends JpaRepository<DatosCrf, Integer> {
    
}