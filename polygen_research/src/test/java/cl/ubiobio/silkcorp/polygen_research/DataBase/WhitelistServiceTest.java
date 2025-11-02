package cl.ubiobio.silkcorp.polygen_research.DataBase;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhitelistServiceTest {

    @Mock
    private WhitelistRepository whitelistRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private WhitelistService whitelistService;

    @Test
    void testCodificacionContraseña() {
        // Creamos una credencial sin guardar
        Whitelist credencialSinGuardar = new Whitelist();
        credencialSinGuardar.setCorreo("nuevo@usuario.com");
        credencialSinGuardar.setContrasena("password123");
        credencialSinGuardar.setUsuario(new Usuario()); // Un usuario asociado

        ArgumentCaptor<Whitelist> whitelistCaptor = ArgumentCaptor.forClass(Whitelist.class);

        when(whitelistRepository.save(any(Whitelist.class))).then(invocation -> {
            return invocation.getArgument(0);
        });

        // Aqui hacemos la llamada al servicio para probar
        whitelistService.saveCredencial(credencialSinGuardar);
        verify(passwordEncoder, never()).encode(anyString());
        verify(whitelistRepository, times(1)).save(whitelistCaptor.capture());

        // Aqui se hace la verificación final de ver que la contraseña no fue encriptada
        Whitelist credencialCapturada = whitelistCaptor.getValue();
        assertEquals("password123", credencialCapturada.getContrasena());
    }
}