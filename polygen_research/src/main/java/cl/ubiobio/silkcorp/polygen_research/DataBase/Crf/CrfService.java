package cl.ubiobio.silkcorp.polygen_research.DataBase.Crf; // O el paquete que corresponda

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hibernate.Hibernate;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrfRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf.DatosCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente.DatosPaciente;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente.DatosPacienteRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfForm;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO; // DTO Nuevo
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenRowDTO;  // DTO Nuevo

@Service
public class CrfService {

    private final CrfRepository crfRepository;
    private final DatosPacienteRepository datosPacienteRepository;
    private final CampoCrfRepository camposCRFRepository;
    // Asumimos que también tienes un DatosCRFRepository

    public CrfService(CrfRepository crfRepository, 
                      DatosPacienteRepository datosPacienteRepository, 
                      CampoCrfRepository camposCRFRepository) {
        this.crfRepository = crfRepository;
        this.datosPacienteRepository = datosPacienteRepository;
        this.camposCRFRepository = camposCRFRepository;
    }

    /**
     * Prepara un nuevo objeto CrfForm para la vista.
     * Busca todos los campos activos y los pre-llena en el DTO.
     */
    public CrfForm prepararNuevoCrfForm() {
        CrfForm form = new CrfForm(); // Crea el DTO (paciente y lista vacíos)
        
        // 1. Buscar todas las "preguntas" activas
        List<CampoCrf> camposActivos = camposCRFRepository.findByActivoTrueOrderByNombre();

        // 2. Por cada "pregunta", crear una "respuesta" vacía
        List<DatosCrf> respuestasVacias = new ArrayList<>();
        for (CampoCrf campo : camposActivos) {
            DatosCrf respuesta = new DatosCrf();
            respuesta.setCampoCrf(campo); // Asocia la pregunta
            respuestasVacias.add(respuesta);
        }

        // 3. Poner las respuestas vacías en el DTO
        form.setDatosCrfList(respuestasVacias);
        return form;
    }

    /**
     * Guarda el formulario completo (Paciente + Crf + Respuestas)
     * en una sola transacción.
     */

    @Transactional
    public void guardarCrfCompleto(CrfForm form) {
        
        // 1. Guardar el Paciente
        DatosPaciente pacienteGuardado = datosPacienteRepository.save(form.getDatosPaciente());

        // 2. Crear el Contenedor CRF y asociarlo al paciente
        Crf crf = new Crf();
        crf.setDatosPaciente(pacienteGuardado);

        // --- ¡VUELVE A AÑADIR ESTAS 3 LÍNEAS! ---
        // Sincronizamos el lado inverso de la relación (Paciente -> CRF)
        // Esto es necesario para que el botón "Ver" funcione de inmediato.
        if (pacienteGuardado.getCrfs() == null) {
            pacienteGuardado.setCrfs(new java.util.ArrayList<>()); // Usa la ruta completa por si acaso
        }
        pacienteGuardado.getCrfs().add(crf);
        // --- FIN DEL ARREGLO ---

        // 3. Procesar y asociar las respuestas
        List<DatosCrf> respuestasDelForm = form.getDatosCrfList();

        for (DatosCrf respuesta : respuestasDelForm) {
            // ... (el resto de tu lógica de guardado)
            if (respuesta.getValor() != null && !respuesta.getValor().trim().isEmpty()) {
                int idCampo = respuesta.getCampoCrf().getIdCampo();
                CampoCrf campoReal = camposCRFRepository.findById(idCampo)
                    .orElseThrow(() -> new RuntimeException("Error: No se encontró el CampoCRF con ID: " + idCampo));
                respuesta.setCampoCrf(campoReal);
                crf.addDato(respuesta);
            }
        }

        // 5. Guardar el CRF
        crfRepository.save(crf);

        datosPacienteRepository.save(pacienteGuardado);
    }

    public List<Crf> getAllCrfs() {
        // Simplemente llama al método findAll() del repositorio de Crf
        return crfRepository.findAll();
    }

    public Optional<Crf> getCrfById(Integer id) {
        return crfRepository.findById(id);
    }



    /**
     * Prepara una vista de "Reporte" pivotando los datos de los CRFs.
     * @return Un DTO que contiene las columnas (Campos) y las filas (Datos)
     */
    @Transactional(readOnly = true)
    public CrfResumenViewDTO getCrfResumenView() {
        
        // 1. Obtener las Columnas (los <th>)
        // (Este método ya lo creamos antes)
        List<CampoCrf> campos = camposCRFRepository.findByActivoTrueOrderByNombre();

        // 2. Obtener las Filas (los <tr>)
        List<Crf> crfs = crfRepository.findAll();
        
        // 3. Preparar el DTO principal
        CrfResumenViewDTO viewDTO = new CrfResumenViewDTO();
        viewDTO.setCamposActivos(campos);
        
        List<CrfResumenRowDTO> filasDTO = new ArrayList<>();
        
        // 4. Bucle principal: Procesar cada CRF (cada fila)
        for (Crf crf : crfs) {
            // Forzamos la carga de datos 'lazy'
            Hibernate.initialize(crf.getDatosPaciente());
            Hibernate.initialize(crf.getDatosCrfList());

            CrfResumenRowDTO rowDTO = new CrfResumenRowDTO();
            rowDTO.setCrf(crf); // Asignamos el CRF (para paciente, fecha, etc.)

            // 5. ¡El Pivote!
            // Creamos un Mapa para buscar valores rápidamente
            Map<Integer, String> valoresMap = new HashMap<>();
            
            for (DatosCrf dato : crf.getDatosCrfList()) {
                if (dato.getCampoCrf() != null) {
                    valoresMap.put(dato.getCampoCrf().getIdCampo(), dato.getValor());
                }
            }
            
            rowDTO.setValores(valoresMap); // Asignamos el mapa de valores
            filasDTO.add(rowDTO);
        }
        
        viewDTO.setFilas(filasDTO);
        return viewDTO;
    }

}