package cl.ubiobio.silkcorp.polygen_research.DataBase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistRepository;
import cl.ubiobio.silkcorp.polygen_research.security.CustomUserDetailService;
import cl.ubiobio.silkcorp.polygen_research.security.CustomUserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @Mock
    private WhitelistRepository whitelistRepository;
    @InjectMocks
    private CustomUserDetailService userDetailService;

    @Test
    void testLoadUserByUsername_CustomUserExiste() {

        RolUsuario rolFalso = new RolUsuario();
        rolFalso.setNombreRol("ADMINISTRADOR"); 

        Usuario usuarioFalso = new Usuario();
        usuarioFalso.setNombreUsuario("Nombre de Prueba Admin");
        usuarioFalso.setRolUsuario(rolFalso);

        Whitelist credencialFalsa = new Whitelist();
        credencialFalsa.setCorreo("admin@test.com");
        credencialFalsa.setContrasena("hash_de_password_abc123");
        credencialFalsa.setUsuario(usuarioFalso); // Vinculamos el usuario

        // Cuando el repositorio busque este correo devuelve la credencial falsa
        when(whitelistRepository.findByCorreo("admin@test.com")).thenReturn(Optional.of(credencialFalsa));

        // Aqui ejecutamos el método a probar
        UserDetails userDetails = userDetailService.loadUserByUsername("admin@test.com");

        assertNotNull(userDetails);
        // Verificar que sea de tipo CustomUserDetails
        assertTrue(userDetails instanceof CustomUserDetails, "Debe ser una instancia de CustomUserDetails");
        assertEquals("admin@test.com", userDetails.getUsername(), "El username debe ser el email");
        assertEquals("hash_de_password_abc123", userDetails.getPassword(), "La contraseña debe ser el hash");
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")));

        CustomUserDetails customUser = (CustomUserDetails) userDetails;
        assertEquals("Nombre de Prueba Admin", customUser.getNombreUsuario(), "Debe cargar el nombreUsuario personalizado");
    }

    // Prueba de un escenario donde el usuario no existe
    @Test
    void testLoadUserByUsername_noExiste() {
        
        // Aqui organizamos el mock para que devuelva vacío
        when(whitelistRepository.findByCorreo("fantasma@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailService.loadUserByUsername("fantasma@test.com");
            // En caso de que no lance la excepción, la prueba fallará 
        }, "Debe lanzar UsernameNotFoundException si el correo no existe"); 

        verify(whitelistRepository, times(1)).findByCorreo("fantasma@test.com");
    }
}