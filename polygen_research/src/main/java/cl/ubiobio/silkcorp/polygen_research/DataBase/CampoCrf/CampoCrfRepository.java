package cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampoCrfRepository extends JpaRepository<CampoCrf, Integer> {
    // Métodos CRUD 
}