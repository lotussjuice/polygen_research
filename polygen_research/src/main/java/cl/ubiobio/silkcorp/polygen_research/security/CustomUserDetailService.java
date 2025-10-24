package cl.ubiobio.silkcorp.polygen_research.security;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// NO importamos 'User' aquí, usaremos el nuestro
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections; 

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final WhitelistRepository whitelistRepository;

    public CustomUserDetailService(WhitelistRepository whitelistRepository) {
        this.whitelistRepository = whitelistRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Spring Security usa "username" genéricamente, pero nosotros usamos email
        Whitelist credencial = whitelistRepository.findByCorreo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + email));

        // Obtener roles (esto se mantiene igual)
        Collection<? extends GrantedAuthority> authorities = Collections.emptyList(); 
        if (credencial.getUsuario() != null && credencial.getUsuario().getRolUsuario() != null) {
            authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + credencial.getUsuario().getRolUsuario().getNombreRol().toUpperCase())
            );
        }

        // --- INICIO DEL CAMBIO ---

        // 1. Obtener el nombreUsuario. 
        // Si el usuario no está seteado, usamos el email como fallback.
        String nombreDeUsuario = credencial.getCorreo(); 
        if (credencial.getUsuario() != null && credencial.getUsuario().getNombreUsuario() != null) {
            nombreDeUsuario = credencial.getUsuario().getNombreUsuario();
        }

        // 2. Retornar NUESTRA clase CustomUserDetails en lugar del 'User' genérico
        // Pasamos el 'email' como 'username' (para Spring) y el 'nombreDeUsuario' (para nosotros)
        return new CustomUserDetails(
                credencial.getCorreo(), 
                credencial.getContrasena(), 
                authorities, 
                nombreDeUsuario // Este es el nuevo campo
        );
        // --- FIN DEL CAMBIO ---
    }
}