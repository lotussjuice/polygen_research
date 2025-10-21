package cl.ubiobio.silkcorp.polygen_research.service;

import cl.ubiobio.silkcorp.polygen_research.entity.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.repository.WhitelistRepository;
import org.springframework.stereotype.Service;
// import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class WhitelistService {

    private final WhitelistRepository whitelistRepository;
    // private final PasswordEncoder passwordEncoder; // Descomentar para seguridad

    public WhitelistService(WhitelistRepository whitelistRepository /*, PasswordEncoder passwordEncoder */) {
        this.whitelistRepository = whitelistRepository;
        // this.passwordEncoder = passwordEncoder;
    }

    public Whitelist saveCredencial(Whitelist credencial) {
        // --- Lógica de Seguridad, por ahora guardar directo
        return whitelistRepository.save(credencial);
    }
}