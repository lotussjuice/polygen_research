package cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampoCrfRepository extends JpaRepository<CampoCrf, Integer> {
    // Spring Data JPA creará la consulta automáticamente
    List<CampoCrf> findByActivoTrueOrderByNombre();
}