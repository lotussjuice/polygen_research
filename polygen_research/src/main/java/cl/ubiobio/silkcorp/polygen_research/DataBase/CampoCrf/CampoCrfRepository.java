package cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CampoCrfRepository extends JpaRepository<CampoCrf, Integer> {

    @Query("SELECT c FROM CampoCrf c LEFT JOIN FETCH c.opciones o WHERE c.activo = true ORDER BY c.seccion ASC, c.nombre ASC, o.orden ASC")
    List<CampoCrf> findByActivoTrueOrderBySeccionAndNombre();

    @Query("SELECT c FROM CampoCrf c LEFT JOIN FETCH c.opciones o WHERE c.idCampo = :id ORDER BY o.orden ASC")
    Optional<CampoCrf> findByIdWithOptions(@Param("id") Integer id);

    List<CampoCrf> findByNombreContainingIgnoreCase(String nombre);
}