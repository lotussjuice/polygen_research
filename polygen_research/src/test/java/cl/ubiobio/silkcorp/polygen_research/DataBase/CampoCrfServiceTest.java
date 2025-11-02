package cl.ubiobio.silkcorp.polygen_research.DataBase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrfRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrfService;

@ExtendWith(MockitoExtension.class)
class CampoCrfServiceTest {

    @Mock
    private CampoCrfRepository campoCrfRepository;

    @InjectMocks
    private CampoCrfService campoCrfService;

    @Test
    void testSaveCampo_crear() {
        
    
        CampoCrf campoNuevo = new CampoCrf();
        campoNuevo.setNombre("Campo de Prueba");
        campoNuevo.setTipo("TEXTO");

        ArgumentCaptor<CampoCrf> campoCaptor = ArgumentCaptor.forClass(CampoCrf.class);

        // Simular el comportamiento del repositorio al guardar
        when(campoCrfRepository.save(any(CampoCrf.class))).then(invocation -> {
            CampoCrf campoGuardado = invocation.getArgument(0);
            campoGuardado.setIdCampo(99); // Simulamos el ID autogenerado
            return campoGuardado;
        });


        CampoCrf resultado = campoCrfService.saveCampo(campoNuevo);

        
        verify(campoCrfRepository, times(1)).save(campoCaptor.capture());
        CampoCrf campoCapturado = campoCaptor.getValue();

    
        assertTrue(campoCapturado.getActivo(), "Un campo nuevo debe guardarse como activo por defecto [en metodo]");

        assertEquals("Campo de Prueba", campoCapturado.getNombre());
        assertNotNull(resultado, "El método debe devolver el objeto guardado");
        assertEquals(99, resultado.getIdCampo(), "El resultado debe tener el ID asignado por el mock");
    }

    @Test
    void testGetAllCampos_returnLista() {
        
        
        CampoCrf campo1 = new CampoCrf();
        campo1.setIdCampo(1);
        campo1.setNombre("Campo A");
        
        CampoCrf campo2 = new CampoCrf();
        campo2.setIdCampo(2);
        campo2.setNombre("Campo B");
        
        List<CampoCrf> listaFalsa = List.of(campo1, campo2);
        
        when(campoCrfRepository.findAll()).thenReturn(listaFalsa);
        List<CampoCrf> resultado = campoCrfService.getAllCampos();

        assertNotNull(resultado);
        
 
        assertEquals(2, resultado.size());
        assertEquals("Campo B", resultado.get(1).getNombre());
        verify(campoCrfRepository, times(1)).findAll();
    }
}