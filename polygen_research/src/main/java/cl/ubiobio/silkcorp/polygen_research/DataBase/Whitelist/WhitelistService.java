package cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist;

import org.springframework.stereotype.Service;
import java.util.List;
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

    public List<Whitelist> getAllCredenciales() {
        return whitelistRepository.findAll();
    }

    public boolean existsByCorreo(String correo) {
        return whitelistRepository.findByCorreo(correo).isPresent();
    }
}