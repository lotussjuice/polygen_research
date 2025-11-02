package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; 
import org.springframework.data.repository.query.Param; 
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatosPacienteRepository extends JpaRepository<DatosPaciente, Integer> {
    
    @Query("SELECT p FROM DatosPaciente p LEFT JOIN FETCH p.crfs c LEFT JOIN FETCH c.datosCrfList d WHERE p.idPaciente = :id")
    Optional<DatosPaciente> findByIdWithCrfs(@Param("id") Integer id);
}