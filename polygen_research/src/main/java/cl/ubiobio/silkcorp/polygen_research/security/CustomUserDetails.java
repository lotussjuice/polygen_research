package cl.ubiobio.silkcorp.polygen_research.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Esta clase extiende el User de Spring Security para almacenar
 * datos adicionales, como el nombreUsuario, en la sesión.
 */
public class CustomUserDetails extends User {

    // 1. Campo adicional que queremos guardar
    private final String nombreUsuario;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String nombreUsuario) {
        
        // 2. Llama al constructor del User original (email, pass, roles)
        super(username, password, authorities);
        
        // 3. Guarda nuestro campo adicional
        this.nombreUsuario = nombreUsuario;
    }

    // 4. Getter público para que Thymeleaf (frontend) pueda leerlo
    public String getNombreUsuario() {
        return nombreUsuario;
    }
}