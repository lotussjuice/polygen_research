package cl.ubiobio.silkcorp.polygen_research.Whitelist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WhitelistRepository extends JpaRepository<Whitelist, Integer> {
}