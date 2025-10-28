package cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist;

import org.springframework.stereotype.Service;
import java.util.List;
// import org.springframework.security.crypto.password.PasswordEncoder; // Si vas a encriptar

@Service
public class WhitelistService {

    private final WhitelistRepository whitelistRepository;
    // private final PasswordEncoder passwordEncoder; // Descomentar para seguridad

    public WhitelistService(WhitelistRepository whitelistRepository /*, PasswordEncoder passwordEncoder */) {
        this.whitelistRepository = whitelistRepository;
        // this.passwordEncoder = passwordEncoder;
    }

    public Whitelist saveCredencial(Whitelist credencial) {
        // AÑADIR LÓGICA DE ENCRIPTACIÓN SI ES NECESARIO AQUÍ
        // if (credencial.getIdLista() == null || !credencial.getContrasena().startsWith("$2a$")) {
        //    credencial.setContrasena(passwordEncoder.encode(credencial.getContrasena()));
        // }
        return whitelistRepository.save(credencial);
    }

    /**
     * Obtiene todas las credenciales, asegurando que la información del usuario
     * y su rol asociado esté cargada (gracias a @EntityGraph en el repositorio).
     */
    public List<Whitelist> getAllCredencialesConUsuario() {
        return whitelistRepository.findAll(); //findAll ahora usa EntityGraph
    }

    // Método original (puede quedarse o eliminarse si no se usa)
    public List<Whitelist> getAllCredenciales() {
        return whitelistRepository.findAll();
    }


    public boolean existsByCorreo(String correo) {
        return whitelistRepository.findByCorreo(correo).isPresent();
    }
}
