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
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenRowDTO; 
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO; 

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
        CrfForm form = new CrfForm();
        form.getDatosPaciente().setEstado("ACTIVO");
        List<CampoCrf> camposActivos = camposCRFRepository.findByActivoTrueOrderByNombre();

        List<DatosCrf> respuestasVacias = new ArrayList<>();
        for (CampoCrf campo : camposActivos) {
            DatosCrf respuesta = new DatosCrf();
            respuesta.setCampoCrf(campo); 
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

        // Mueve los nuevos datos del DTO a la Entidad 
        crf.setCasoEstudio(form.isEsCasoEstudio());
        crf.setObservacion(form.getObservacion());
        crf.setEstado("ACTIVO");
        
        if (pacienteGuardado.getCrfs() == null) {
            pacienteGuardado.setCrfs(new java.util.ArrayList<>());
        }
        pacienteGuardado.getCrfs().add(crf);

        // Procesa y asocia las respuestas 
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
                    dato -> dato.getCampoCrf().getIdCampo(), 
                    dato -> dato                        
                ));


        CrfForm form = new CrfForm();
        form.setIdCrf(crf.getIdCrf());
        form.setDatosPaciente(crf.getDatosPaciente());

        // Carga los datos simples del CRF (observaciones, tipo estudio) al DTO
        form.setEsCasoEstudio(crf.isCasoEstudio());
        form.setObservacion(crf.getObservacion());

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
    @Transactional
    public void actualizarCrfCompleto(CrfForm form) {
        
        // Validar que los IDs existen
        Integer crfId = form.getIdCrf();
        
        // Corregido para usar 'id_Paciente' (basado en tu HTML)
        Integer pacienteId = form.getDatosPaciente().getIdPaciente(); 
        
        if (crfId == null || pacienteId == null) {
            throw new RuntimeException("Error: Se intentó actualizar un CRF o Paciente sin ID.");
        }
        
        DatosPaciente pacienteGuardado = datosPacienteRepository.save(form.getDatosPaciente());

        Crf crf = crfRepository.findById(crfId)
                .orElseThrow(() -> new RuntimeException("CRF no encontrado con ID: " + crfId));
                
        crf.setDatosPaciente(pacienteGuardado);

        // Mueve los nuevos datos del DTO a la Entidad
        crf.setCasoEstudio(form.isEsCasoEstudio());
        crf.setObservacion(form.getObservacion());
        
        crf.getDatosCrfList().clear();

        procesarYAdjuntarRespuestas(form.getDatosCrfList(), crf);

        crfRepository.save(crf);
        
        // Registra la actualización
        registroService.logCrfActivity("ACTUALIZACION_CRF", crf);
    }

    private void procesarYAdjuntarRespuestas(List<DatosCrf> respuestasDelForm, Crf crf) {
        if (respuestasDelForm == null) return;
        
        for (DatosCrf respuestaForm : respuestasDelForm) {
            
            if (respuestaForm.getCampoCrf() == null || respuestaForm.getCampoCrf().getIdCampo() == null) {
                continue;
            }

            int idCampo = respuestaForm.getCampoCrf().getIdCampo();
            CampoCrf campoReal = camposCRFRepository.findById(idCampo)
                    .orElseThrow(() -> new RuntimeException("Error: No se encontró el CampoCRF con ID: " + idCampo));

            boolean guardarEsteDato = false;
            String valorFinal = null;
            String valorOriginal = respuestaForm.getValor(); 

            if ("SI/NO".equals(campoReal.getTipo())) {
                if ("1".equals(valorOriginal)) {
                    valorFinal = "1";
                    guardarEsteDato = true;
                } else if ("0".equals(valorOriginal)) {
                    valorFinal = "0";
                    guardarEsteDato = true;
                } else {
                    guardarEsteDato = false; 
                }
            } else { 
                if (valorOriginal != null && !valorOriginal.trim().isEmpty()) {
                    valorFinal = valorOriginal.trim();
                    guardarEsteDato = true;
                }
            }


            if (guardarEsteDato) {
                DatosCrf nuevaRespuesta = new DatosCrf();
                nuevaRespuesta.setValor(valorFinal);
                nuevaRespuesta.setCampoCrf(campoReal);
                crf.addDato(nuevaRespuesta);
            }
        }
    }

    @Transactional(readOnly = true)
    public CrfResumenViewDTO getCrfResumenView(String codigoPaciente) { 

        List<CampoCrf> campos = camposCRFRepository.findByActivoTrueOrderByNombre();
        List<Crf> crfs; 

        if (codigoPaciente != null && !codigoPaciente.trim().isEmpty()) {
            Optional<Crf> crfOptional = crfRepository.findByDatosPacienteCodigoPacienteIgnoreCase(codigoPaciente.trim());
            crfs = crfOptional.map(Collections::singletonList).orElseGet(Collections::emptyList);
        } else {

            crfs = crfRepository.findAll();
        }

        CrfResumenViewDTO viewDTO = new CrfResumenViewDTO();
        viewDTO.setCamposActivos(campos);
        List<CrfResumenRowDTO> filasDTO = new ArrayList<>();

        for (Crf crf : crfs) {
            Hibernate.initialize(crf.getDatosPaciente());
            Hibernate.initialize(crf.getDatosCrfList());
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