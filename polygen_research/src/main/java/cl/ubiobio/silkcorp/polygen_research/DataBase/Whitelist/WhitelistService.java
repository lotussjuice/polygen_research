package cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


@Service
public class WhitelistService {

    private final WhitelistRepository whitelistRepository;
 

    public WhitelistService(WhitelistRepository whitelistRepository) {
        this.whitelistRepository = whitelistRepository;
        // this.passwordEncoder = passwordEncoder;
    }

    public Whitelist saveCredencial(Whitelist credencial) {
        return whitelistRepository.save(credencial);
    }

    // Carga todas las credenciales 
    public List<Whitelist> getAllCredencialesConUsuario() {
        return whitelistRepository.findAll(); 
    }

    public List<Whitelist> getAllCredenciales() {
        return whitelistRepository.findAll();
    }

    public boolean existsByCorreo(String correo) {
        return whitelistRepository.findByCorreo(correo).isPresent();
    }

    public Optional<Whitelist> findByCorreo(String correo) {
        return whitelistRepository.findByCorreo(correo);
    }
}
