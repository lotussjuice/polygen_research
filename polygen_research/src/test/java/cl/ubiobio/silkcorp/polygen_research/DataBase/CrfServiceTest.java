package cl.ubiobio.silkcorp.polygen_research.DataBase;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrfRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente.DatosPacienteRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad.RegistroActividadService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfForm;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.Crf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.CrfService;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
// Mockito se emplea para crear objetos simulados (mocks) y probar
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrfServiceTest {

    // Mock de cada repositorio que CrfService usa internamente
    @Mock
    private CrfRepository crfRepository;
    @Mock
    private DatosPacienteRepository datosPacienteRepository;
    @Mock
    private CampoCrfRepository camposCRFRepository;
    @Mock
    private RegistroActividadService registroService; // También "mockeamos" otros servicios

   // Creacion de instancia de CrfService con los mocks inyectados
    @InjectMocks
    private CrfService crfService;


    // Prueba unitaria para el método prepararNuevoCrfForm
    @Test
    void testPrepararNuevoCrfForm() {

        
        CampoCrf campo1 = new CampoCrf(); 
        campo1.setIdCampo(1);
        campo1.setNombre("Edad");

        CampoCrf campo2 = new CampoCrf(); 
        campo2.setIdCampo(2);
        campo2.setNombre("Tabaquismo");

        List<CampoCrf> listaCamposFalsos = List.of(campo1, campo2);

        // Preparando mock
        when(camposCRFRepository.findByActivoTrueOrderByNombre()).thenReturn(listaCamposFalsos);

        CrfForm formResultado = crfService.prepararNuevoCrfForm();

        // Verificaciones de distinto tipo
        assertNotNull(formResultado, "El formulario no debería ser nulo");
        
        assertNotNull(formResultado.getDatosPaciente(), "DatosPaciente no debería ser nulo");
        assertEquals("ACTIVO", formResultado.getDatosPaciente().getEstado(), "El estado del paciente debe ser ACTIVO");

        assertNotNull(formResultado.getDatosCrfList(), "La lista de respuestas no debería ser nula");
        assertEquals(2, formResultado.getDatosCrfList().size(), "La lista de respuestas debe tener 2 elementos");

        assertEquals("Edad", formResultado.getDatosCrfList().get(0).getCampoCrf().getNombre());
        
    }


    // Verificando métodos de busqueda 
    @Test
    void testGetCrfById() {
        Crf crfFalso = new Crf();
        crfFalso.setIdCrf(123);
        crfFalso.setEstado("ACTIVO");

   
        when(crfRepository.findById(123)).thenReturn(Optional.of(crfFalso));
        Optional<Crf> resultado = crfService.getCrfById(123);

        // Verificaciones
        assertTrue(resultado.isPresent(), "El resultado debe estar presente");
        assertEquals(123, resultado.get().getIdCrf(), "El ID del CRF debe ser 123");
        
        // Verificamos que el repositorio fue llamado 1 vez
        verify(crfRepository, times(1)).findById(123);
    }

    @Test
    void testGetCrfById_CuandoNoSeEncuentra_DebeDevolverVacio() {
        when(crfRepository.findById(404)).thenReturn(Optional.empty()); // Devolvemos un Optional vacío

        Optional<Crf> resultado = crfService.getCrfById(404);
        assertFalse(resultado.isPresent(), "El resultado debe estar vacío (not present)");
    }
}