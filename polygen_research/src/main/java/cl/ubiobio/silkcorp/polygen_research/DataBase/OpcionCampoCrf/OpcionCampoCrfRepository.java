package cl.ubiobio.silkcorp.polygen_research.DataBase.OpcionCampoCrf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpcionCampoCrfRepository extends JpaRepository<OpcionCampoCrf, Integer> {
}