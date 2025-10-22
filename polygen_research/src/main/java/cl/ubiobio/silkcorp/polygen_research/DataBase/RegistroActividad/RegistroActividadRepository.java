package cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroActividadRepository extends JpaRepository<RegistroActividad, Integer> {
}