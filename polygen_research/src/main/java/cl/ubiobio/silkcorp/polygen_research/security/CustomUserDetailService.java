package cl.ubiobio.silkcorp.polygen_research.security;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistRepository;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// NO importamos 'User' aquí, usaremos el nuestro
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final WhitelistRepository whitelistRepository;

    public CustomUserDetailService(WhitelistRepository whitelistRepository) {
        this.whitelistRepository = whitelistRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Whitelist credencial = whitelistRepository.findByCorreo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + email));
        Usuario usuario = credencial.getUsuario();

 
        if (usuario == null || "ELIMINADO".equals(usuario.getEstado())) {
            throw new DisabledException("Esta cuenta ha sido eliminada y no tiene acceso.");
        }

        // Obtener roles
        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
        if (usuario.getRolUsuario() != null) {
            authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + usuario.getRolUsuario().getNombreRol().toUpperCase()));
        }

        // Obtener el nombre real para la UI
        String nombreDeUsuario = credencial.getCorreo(); // Fallback al correo
        if (usuario.getNombreUsuario() != null && !usuario.getNombreUsuario().isEmpty()) {
            nombreDeUsuario = usuario.getNombreUsuario();
        }

        return new CustomUserDetails(
                credencial.getCorreo(),
                credencial.getContrasena(),
                authorities,
                nombreDeUsuario // Pasamos el nombre real para mostrarlo en el Dashboard
        );
    }
}
