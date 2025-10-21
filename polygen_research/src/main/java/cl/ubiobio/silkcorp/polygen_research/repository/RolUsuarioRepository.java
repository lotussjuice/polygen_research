package cl.ubiobio.silkcorp.polygen_research.repository;

import cl.ubiobio.silkcorp.polygen_research.entity.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolUsuarioRepository extends JpaRepository<RolUsuario, Integer> {
}