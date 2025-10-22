package cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WhitelistRepository extends JpaRepository<Whitelist, Integer> {
    Optional<Whitelist> findByCorreo(String correo);
}