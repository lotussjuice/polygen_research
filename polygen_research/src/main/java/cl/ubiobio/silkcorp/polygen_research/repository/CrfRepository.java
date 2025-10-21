package cl.ubiobio.silkcorp.polygen_research.repository;

import cl.ubiobio.silkcorp.polygen_research.entity.Crf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrfRepository extends JpaRepository<Crf, Integer> {

    // List<Crf> findByDatosPaciente_IdPaciente(Integer pacienteId);
}