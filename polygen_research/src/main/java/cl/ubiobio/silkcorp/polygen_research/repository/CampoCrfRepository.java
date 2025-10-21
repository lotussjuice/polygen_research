package cl.ubiobio.silkcorp.polygen_research.repository;

import cl.ubiobio.silkcorp.polygen_research.entity.CampoCrf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampoCrfRepository extends JpaRepository<CampoCrf, Integer> {
    // Métodos CRUD 
}