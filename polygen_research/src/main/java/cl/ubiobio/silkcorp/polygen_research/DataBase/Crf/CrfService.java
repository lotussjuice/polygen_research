package cl.ubiobio.silkcorp.polygen_research.DataBase.Crf; // O el paquete que corresponda

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf.CampoCrfRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf.DatosCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente.DatosPaciente;
import cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente.DatosPacienteRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad.RegistroActividadService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfForm;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenRowDTO; // DTO Nuevo
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO; // DTO Nuevo

@Service
public class CrfService {

    private final CrfRepository crfRepository;
    private final DatosPacienteRepository datosPacienteRepository;
    private final CampoCrfRepository camposCRFRepository;

    @Autowired
    private RegistroActividadService registroService;

    public CrfService(CrfRepository crfRepository,
            DatosPacienteRepository datosPacienteRepository,
            CampoCrfRepository camposCRFRepository) {
        this.crfRepository = crfRepository;
        this.datosPacienteRepository = datosPacienteRepository;
        this.camposCRFRepository = camposCRFRepository;
    }


    public CrfForm prepararNuevoCrfForm() {
        CrfForm form = new CrfForm(); // Crea el DTO (paciente y lista vacíos)
        form.getDatosPaciente().setEstado("ACTIVO");
        List<CampoCrf> camposActivos = camposCRFRepository.findByActivoTrueOrderByNombre();

        List<DatosCrf> respuestasVacias = new ArrayList<>();
        for (CampoCrf campo : camposActivos) {
            DatosCrf respuesta = new DatosCrf();
            respuesta.setCampoCrf(campo); // Asocia la pregunta
            respuestasVacias.add(respuesta);
        }
        form.setDatosCrfList(respuestasVacias);
        return form;
    }

    @Transactional
    public void guardarCrfCompleto(CrfForm form) {

        // Guardar o actualizar Paciente
        DatosPaciente pacienteGuardado = datosPacienteRepository.save(form.getDatosPaciente());

        // Crear el Contenedor CRF y asociarlo al paciente
        Crf crf = new Crf();
        crf.setDatosPaciente(pacienteGuardado);

        // --- Mueve los nuevos datos del DTO a la Entidad ---
        crf.setCasoEstudio(form.isEsCasoEstudio());
        crf.setObservacion(form.getObservacion());
        crf.setEstado("ACTIVO");
        
        if (pacienteGuardado.getCrfs() == null) {
            pacienteGuardado.setCrfs(new java.util.ArrayList<>());
        }
        pacienteGuardado.getCrfs().add(crf);

        // --- Procesa y asocia las respuestas ---
        // (Esta lógica ahora está en un método helper, ver más abajo)
        procesarYAdjuntarRespuestas(form.getDatosCrfList(), crf);

        crfRepository.save(crf);
        registroService.logCrfActivity("CREACION_CRF", crf);
        datosPacienteRepository.save(pacienteGuardado);
    }

    public List<Crf> getAllCrfs() {
        return crfRepository.findAll();
    }

    public Optional<Crf> getCrfById(Integer id) {
        return crfRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public CrfResumenViewDTO getCrfResumenView() {
        List<CampoCrf> campos = camposCRFRepository.findByActivoTrueOrderByNombre();
        List<Crf> crfs = crfRepository.findAll();
        CrfResumenViewDTO viewDTO = new CrfResumenViewDTO();
        viewDTO.setCamposActivos(campos);
        List<CrfResumenRowDTO> filasDTO = new ArrayList<>();

        for (Crf crf : crfs) {
            Hibernate.initialize(crf.getDatosPaciente());
            Hibernate.initialize(crf.getDatosCrfList());

            CrfResumenRowDTO rowDTO = new CrfResumenRowDTO();
            rowDTO.setCrf(crf); 

            Map<Integer, String> valoresMap = new HashMap<>();
            for (DatosCrf dato : crf.getDatosCrfList()) {
                if (dato.getCampoCrf() != null) {
                    valoresMap.put(dato.getCampoCrf().getIdCampo(), dato.getValor());
                }
            }
            rowDTO.setValores(valoresMap);
            filasDTO.add(rowDTO);
        }
        viewDTO.setFilas(filasDTO);
        return viewDTO;
    }


    // --- MÉTODO MODIFICADO ---
    @Transactional(readOnly = true)
    public CrfForm prepararCrfFormParaEditar(Integer crfId) {
        // Busqueda de CRF por ID
        Crf crf = crfRepository.findById(crfId)
                .orElseThrow(() -> new RuntimeException("CRF no encontrado con ID: " + crfId));
        
        Hibernate.initialize(crf.getDatosPaciente());
        Hibernate.initialize(crf.getDatosCrfList());

        // Convierte las respuestas guardadas en un Mapa para búsqueda rápida
        Map<Integer, DatosCrf> respuestasGuardadas = crf.getDatosCrfList().stream()
                .collect(Collectors.toMap(
                    dato -> dato.getCampoCrf().getIdCampo(), // Clave
                    dato -> dato                           // Valor
                ));


        CrfForm form = new CrfForm();
        form.setIdCrf(crf.getIdCrf());
        form.setDatosPaciente(crf.getDatosPaciente());

        // --- ¡CAMBIO AÑADIDO! ---
        // Carga los datos simples del CRF (observaciones, tipo estudio) al DTO
        form.setEsCasoEstudio(crf.isCasoEstudio());
        form.setObservacion(crf.getObservacion());
        // --- FIN DEL CAMBIO ---

        // Carga TODOS los campos activos (igual que en /nuevo)
        List<CampoCrf> todosCamposActivos = camposCRFRepository.findByActivoTrueOrderByNombre();

        // Construye la lista final para el formulario
        List<DatosCrf> listaParaForm = new ArrayList<>();
        for (CampoCrf campo : todosCamposActivos) {
            DatosCrf respuesta = respuestasGuardadas.get(campo.getIdCampo());

            if (respuesta != null) {
                listaParaForm.add(respuesta);
            } else {
                DatosCrf respuestaVacia = new DatosCrf();
                respuestaVacia.setCampoCrf(campo);
                listaParaForm.add(respuestaVacia);
            }
        }
        
        form.setDatosCrfList(listaParaForm);
        return form;
    }


    // --- MÉTODO MODIFICADO ---
    @Transactional
    public void actualizarCrfCompleto(CrfForm form) {
        
        // 1. Validar que los IDs existen
        Integer crfId = form.getIdCrf();
        
        // --- ¡CAMBIO AÑADIDO! --- 
        // Corregido para usar 'id_Paciente' (basado en tu HTML)
        Integer pacienteId = form.getDatosPaciente().getIdPaciente(); 
        
        if (crfId == null || pacienteId == null) {
            throw new RuntimeException("Error: Se intentó actualizar un CRF o Paciente sin ID.");
        }
        
        // 2. Actualizar el Paciente
        DatosPaciente pacienteGuardado = datosPacienteRepository.save(form.getDatosPaciente());

        // 3. Buscar el CRF existente
        Crf crf = crfRepository.findById(crfId)
                .orElseThrow(() -> new RuntimeException("CRF no encontrado con ID: " + crfId));
                
        // 4. Actualizar los datos simples del CRF
        crf.setDatosPaciente(pacienteGuardado);

        // --- ¡CAMBIO AÑADIDO! ---
        // Mueve los nuevos datos del DTO a la Entidad
        crf.setCasoEstudio(form.isEsCasoEstudio());
        crf.setObservacion(form.getObservacion());
        // (El 'estado' no se toca, se mantiene el original)
        // --- FIN DEL CAMBIO ---

        // 5. ¡LÓGICA DE ACTUALIZACIÓN DE RESPUESTAS (Clear and Reload)!
        
        // 5a. Limpia la lista vieja.
        // (Gracias a 'orphanRemoval=true', esto borrará los 'DatosCRF' viejos de la BD)
        crf.getDatosCrfList().clear();

        // 5b. Recarga la lista con los nuevos datos del formulario
        // --- ¡CAMBIO AÑADIDO! ---
        // (Reutiliza la misma lógica exacta de 'guardarCrfCompleto')
        procesarYAdjuntarRespuestas(form.getDatosCrfList(), crf);

        // 6. Guardar todo
        crfRepository.save(crf);
        
        // --- ¡CAMBIO AÑADIDO! ---
        // Registra la actualización
        registroService.logCrfActivity("ACTUALIZACION_CRF", crf);
    }


    /**
     * MÉTODO HELPER PRIVADO
     * (Extraído de tu 'guardarCrfCompleto' para reutilizarlo en 'actualizarCrfCompleto')
     * * Procesa la lista de respuestas del formulario, maneja los tipos "SI/NO",
     * re-hidrata las entidades CampoCrf y las adjunta al CRF principal.
     */
    private void procesarYAdjuntarRespuestas(List<DatosCrf> respuestasDelForm, Crf crf) {
        for (DatosCrf respuesta : respuestasDelForm) {
            // Re-hidrata la entidad 'CampoCrf'
            int idCampo = respuesta.getCampoCrf().getIdCampo();
            CampoCrf campoReal = camposCRFRepository.findById(idCampo)
                    .orElseThrow(() -> new RuntimeException("Error: No se encontró el CampoCRF con ID: " + idCampo));
            respuesta.setCampoCrf(campoReal); 

            boolean guardarEsteDato = false;
            String valorFinal = null;

            if ("SI/NO".equals(campoReal.getTipo())) {
                // Lógica de SI/NO: '1' si está marcado, '0' si es nulo o no es '1'
                valorFinal = "1".equals(respuesta.getValor()) ? "1" : "0"; 
                guardarEsteDato = true; // Siempre guardar SI/NO (como 0 o 1)
            } else {
                // Para otros tipos, solo guardamos si no está vacío
                if (respuesta.getValor() != null && !respuesta.getValor().trim().isEmpty()) {
                    valorFinal = respuesta.getValor().trim();
                    guardarEsteDato = true;
                }
            }

            if (guardarEsteDato) {
                respuesta.setValor(valorFinal); // Asignamos el valor procesado
                
                // IMPORTANTE: Resetea el ID del 'DatoCRF'
                // Esto le dice a JPA que es una NUEVA fila.
                // (Asumiendo que el ID se llama 'idDetalle' según tu código anterior)
                respuesta.setIdDetalle(null); 
                
                crf.addDato(respuesta); // Añadimos al CRF (esto setea la FK)
            }
        }
    }

    @Transactional(readOnly = true)
    public CrfResumenViewDTO getCrfResumenView(String codigoPaciente) { // Parameter is still String

        List<CampoCrf> campos = camposCRFRepository.findByActivoTrueOrderByNombre();
        List<Crf> crfs; // Still use a List for processing later

        // --- LÓGICA DE FILTRADO AJUSTADA ---
        if (codigoPaciente != null && !codigoPaciente.trim().isEmpty()) {
            // Llama al nuevo método del repositorio que devuelve Optional
            Optional<Crf> crfOptional = crfRepository.findByDatosPacienteCodigoPacienteIgnoreCase(codigoPaciente.trim());
            // Convierte el Optional a una lista (vacía o con un elemento)
            crfs = crfOptional.map(Collections::singletonList).orElseGet(Collections::emptyList);
        } else {
            // Si no hay búsqueda, obtén todos
            crfs = crfRepository.findAll();
        }

        CrfResumenViewDTO viewDTO = new CrfResumenViewDTO();
        viewDTO.setCamposActivos(campos);
        List<CrfResumenRowDTO> filasDTO = new ArrayList<>();

        for (Crf crf : crfs) {
            Hibernate.initialize(crf.getDatosPaciente());
            Hibernate.initialize(crf.getDatosCrfList());
            // ... (crear rowDTO, llenar valoresMap, etc.) ...
            CrfResumenRowDTO rowDTO = new CrfResumenRowDTO();
            rowDTO.setCrf(crf);
            Map<Integer, String> valoresMap = crf.getDatosCrfList().stream()
                    .filter(dato -> dato.getCampoCrf() != null)
                    .collect(Collectors.toMap(
                        dato -> dato.getCampoCrf().getIdCampo(),
                        DatosCrf::getValor,
                        (v1, v2) -> v1
                    ));
            rowDTO.setValores(valoresMap);
            filasDTO.add(rowDTO);
        }

        viewDTO.setFilas(filasDTO);
        return viewDTO;
    }
}