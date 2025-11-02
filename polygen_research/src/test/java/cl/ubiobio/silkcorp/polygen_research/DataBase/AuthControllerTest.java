package cl.ubiobio.silkcorp.polygen_research.DataBase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuarioService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.UsuarioService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistService;
import cl.ubiobio.silkcorp.polygen_research.auth.AuthController;
import cl.ubiobio.silkcorp.polygen_research.auth.RegisterDto;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Prueba unitaria para la logica del AuthController.processRegistration
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UsuarioService usuarioService;
    @Mock
    private WhitelistService whitelistService;
    @Mock
    private RolUsuarioService rolUsuarioService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BindingResult mockBindingResult;
    @Mock
    private RedirectAttributes mockRedirectAttributes;

    // Creamos la instancia del controlador con los Mocks inyectados
    @InjectMocks
    private AuthController authController;

    // Prueba del método processRegistration en el caso exitoso
    @Test
    void testProcessRegistration_Success() {

        RegisterDto dto = new RegisterDto();
        dto.setCorreo("test@example.com");
        dto.setNombreUsuario("Test User");
        dto.setPassword("password123");

        RolUsuario rolVisitanteFalso = new RolUsuario();
        rolVisitanteFalso.setNombreRol("VISITANTE");

        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setIdUsuario(123);
        usuarioGuardado.setNombreUsuario("Test User");

        when(mockBindingResult.hasErrors()).thenReturn(false);  
        when(rolUsuarioService.getRolVisitantePorDefecto()).thenReturn(rolVisitanteFalso);
        when(usuarioService.saveUsuario(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(passwordEncoder.encode("password123")).thenReturn("hash_abc123");
        
        // Aqui ejecutamos el método a probar 
        String viewName = authController.processRegistration(dto, mockBindingResult, mockRedirectAttributes);

      
        assertEquals("redirect:/login", viewName);

        verify(usuarioService, times(1)).saveUsuario(any(Usuario.class));
        verify(whitelistService, times(1)).saveCredencial(any(Whitelist.class));
        verify(mockRedirectAttributes, times(1)).addFlashAttribute("successMessage", "¡Usuario registrado con éxito! Por favor, inicie sesión.");
    }
}