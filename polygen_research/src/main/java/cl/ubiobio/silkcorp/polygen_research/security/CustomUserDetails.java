package cl.ubiobio.silkcorp.polygen_research.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;


public class CustomUserDetails extends User {

    private final String nombreUsuario;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String nombreUsuario) {
        
        super(username, password, authorities);
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}