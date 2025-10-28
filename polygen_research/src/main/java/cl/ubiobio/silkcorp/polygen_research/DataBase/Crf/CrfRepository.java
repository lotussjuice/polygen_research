package cl.ubiobio.silkcorp.polygen_research.DataBase.Crf;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrfRepository extends JpaRepository<Crf, Integer> {

    Optional<Crf> findByDatosPacienteCodigoPacienteIgnoreCase(String codigo);

    // Cuenta cuántos CRFs tienen el campo 'esCasoEstudio' en true
    long countByEsCasoEstudioTrue();
    
    // Cuenta cuántos CRFs tienen el campo 'esCasoEstudio' en false
    long countByEsCasoEstudioFalse();
}