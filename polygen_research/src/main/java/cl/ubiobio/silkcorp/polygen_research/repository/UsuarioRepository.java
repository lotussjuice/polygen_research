package cl.ubiobio.silkcorp.polygen_research.repository;

import cl.ubiobio.silkcorp.polygen_research.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
}