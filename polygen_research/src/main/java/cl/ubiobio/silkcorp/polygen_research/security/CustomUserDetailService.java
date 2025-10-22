package cl.ubiobio.silkcorp.polygen_research.security;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections; // Import corrected

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final WhitelistRepository whitelistRepository;

    public CustomUserDetailService(WhitelistRepository whitelistRepository) {
        this.whitelistRepository = whitelistRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Spring Security uses "username" generically, but we use email
        Whitelist credencial = whitelistRepository.findByCorreo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + email));

        // Get authorities (roles) from the associated Usuario entity
        Collection<? extends GrantedAuthority> authorities = Collections.emptyList(); // Default to no roles
        if (credencial.getUsuario() != null && credencial.getUsuario().getRolUsuario() != null) {
            // Assign role like "ROLE_ADMINISTRADOR", "ROLE_INVESTIGADOR", etc.
            authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + credencial.getUsuario().getRolUsuario().getNombreRol().toUpperCase())
            );
        }

        // Return Spring Security's User object
        return new User(credencial.getCorreo(), credencial.getContrasena(), authorities);
    }
}