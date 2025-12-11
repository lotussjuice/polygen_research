package cl.ubiobio.silkcorp.polygen_research.DataBase.Crf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import cl.ubiobio.silkcorp.polygen_research.DataBase.OpcionCampoCrf.OpcionCampoCrf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad.RegistroActividadService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CampoCrfStatsDTO;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfDetalleDTO;
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
        
        // USO DEL NUEVO MÉTODO ORDENADO
        List<CampoCrf> camposActivos = camposCRFRepository.findByActivoTrueOrderBySeccionAndNombre();

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
        DatosPaciente pacienteGuardado = datosPacienteRepository.save(form.getDatosPaciente());

        Crf crf = new Crf();
        crf.setDatosPaciente(pacienteGuardado);
        crf.setCasoEstudio(form.isEsCasoEstudio());
        crf.setObservacion(form.getObservacion());
        crf.setEstado("ACTIVO");

        if (pacienteGuardado.getCrfs() == null) {
            pacienteGuardado.setCrfs(new ArrayList<>());
        }
        pacienteGuardado.getCrfs().add(crf);

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
    public CrfResumenViewDTO getCrfResumenView(String codigoPaciente, boolean soloActivos) {
        // USO DEL NUEVO MÉTODO ORDENADO
        List<CampoCrf> campos = camposCRFRepository.findByActivoTrueOrderBySeccionAndNombre();
        List<Crf> crfs;

        if (codigoPaciente != null && !codigoPaciente.trim().isEmpty()) {
            crfs = crfRepository.findByDatosPacienteCodigoPacienteContainingIgnoreCase(codigoPaciente.trim());
        } else {
            crfs = crfRepository.findAll();
        }

        if (soloActivos) {
            crfs = crfs.stream()
                    .filter(c -> c.getDatosPaciente() != null
                            && "ACTIVO".equalsIgnoreCase(c.getDatosPaciente().getEstado()))
                    .collect(Collectors.toList());
        }

        return construirResumenView(campos, crfs);
    }

    public CrfResumenViewDTO getCrfResumenView(boolean soloActivos) {
        return getCrfResumenView(null, soloActivos);
    }

    private CrfResumenViewDTO construirResumenView(List<CampoCrf> campos, List<Crf> crfs) {
        CrfResumenViewDTO viewDTO = new CrfResumenViewDTO();

        List<CampoCrfStatsDTO> columnasStats = new ArrayList<>();

        for (CampoCrf campo : campos) {
            String colKey = String.valueOf(campo.getIdCampo());
            String colName = campo.getNombre();
            // AQUI AGREGAMOS LA SECCION AL NOMBRE DE COLUMNA SI GUSTAS, O LO DEJAMOS PARA EL FRONT
            columnasStats.add(new CampoCrfStatsDTO(campo, null, colKey, colName, 0, 0, 0));
        }

        List<CrfResumenRowDTO> filasDTO = new ArrayList<>();

        for (Crf crf : crfs) {
            Hibernate.initialize(crf.getDatosPaciente());
            Hibernate.initialize(crf.getDatosCrfList());

            CrfResumenRowDTO rowDTO = new CrfResumenRowDTO();
            rowDTO.setCrf(crf);

            Map<Integer, String> respuestasCrudas = new HashMap<>();
            for (DatosCrf dato : crf.getDatosCrfList()) {
                if (dato.getCampoCrf() != null) {
                    respuestasCrudas.put(dato.getCampoCrf().getIdCampo(), dato.getValor());
                }
            }

            Map<String, String> valoresFila = new HashMap<>();
            int contadorFaltantes = 0;

            for (CampoCrfStatsDTO col : columnasStats) {
                CampoCrf campo = col.getCampoCrf();
                Integer campoId = campo.getIdCampo();
                String valorCrudo = respuestasCrudas.get(campoId);
                String valorCelda = "-";

                if (valorCrudo != null && !valorCrudo.trim().isEmpty()) {
                    if ("SI/NO".equals(campo.getTipo())) {
                        if ("1".equals(valorCrudo)) {
                            valorCelda = "SÍ";
                        } else if ("0".equals(valorCrudo)) {
                            valorCelda = "NO";
                        } else {
                            valorCelda = valorCrudo;
                        }

                    } else if ("SELECCION_UNICA".equals(campo.getTipo())) {
                        if (campo.getOpciones() != null) {
                            String etiquetaEncontrada = valorCrudo;
                            for (OpcionCampoCrf op : campo.getOpciones()) {
                                if (String.valueOf(op.getOrden()).equals(valorCrudo)) {
                                    etiquetaEncontrada = op.getEtiqueta();
                                    break;
                                }
                            }
                            valorCelda = etiquetaEncontrada;
                        } else {
                            valorCelda = valorCrudo;
                        }
                    } else {
                        valorCelda = valorCrudo;
                    }

                } else {
                    contadorFaltantes++;
                }

                valoresFila.put(col.getColumnaKey(), valorCelda);
            }

            rowDTO.setDatosFaltantes(contadorFaltantes);
            rowDTO.setValores(valoresFila);
            filasDTO.add(rowDTO);
        }

        for (CampoCrfStatsDTO col : columnasStats) {
            long countVacios = 0;
            String key = col.getColumnaKey();

            for (CrfResumenRowDTO fila : filasDTO) {
                String valor = fila.getValores().get(key);
                if (valor == null || valor.trim().isEmpty() || valor.equals("-")) {
                    countVacios++;
                }
            }
            col.setCountVacios(countVacios);
        }

        viewDTO.setCamposConStats(columnasStats);
        viewDTO.setFilas(filasDTO);
        return viewDTO;
    }

    @Transactional(readOnly = true)
    public CrfForm prepararCrfFormParaEditar(Integer crfId) {
        Crf crf = crfRepository.findById(crfId)
                .orElseThrow(() -> new RuntimeException("CRF no encontrado con ID: " + crfId));

        Hibernate.initialize(crf.getDatosPaciente());
        Hibernate.initialize(crf.getDatosCrfList());

        Map<Integer, DatosCrf> respuestasGuardadas = crf.getDatosCrfList().stream()
                .collect(Collectors.toMap(
                        dato -> dato.getCampoCrf().getIdCampo(),
                        dato -> dato));

        CrfForm form = new CrfForm();
        form.setIdCrf(crf.getIdCrf());
        form.setDatosPaciente(crf.getDatosPaciente());
        form.setEsCasoEstudio(crf.isCasoEstudio());
        form.setObservacion(crf.getObservacion());

        // USO DEL NUEVO MÉTODO ORDENADO
        List<CampoCrf> todosCamposActivos = camposCRFRepository.findByActivoTrueOrderBySeccionAndNombre();
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
        Integer crfId = form.getIdCrf();

        if (crfId == null) {
            throw new RuntimeException("Error: Se intentó actualizar un CRF sin ID.");
        }

        Crf crf = crfRepository.findById(crfId)
                .orElseThrow(() -> new RuntimeException("CRF no encontrado con ID: " + crfId));

        DatosPaciente paciente = crf.getDatosPaciente();
        DatosPaciente formPaciente = form.getDatosPaciente();

        paciente.setCodigoPaciente(formPaciente.getCodigoPaciente());
        paciente.setNombre(formPaciente.getNombre());
        paciente.setApellido(formPaciente.getApellido());
        paciente.setNumero(formPaciente.getNumero());
        paciente.setDireccion(formPaciente.getDireccion());

        crf.setCasoEstudio(form.isEsCasoEstudio());
        crf.setObservacion(form.getObservacion());

        Map<Integer, DatosCrf> datosExistentesMap = crf.getDatosCrfList().stream()
                .collect(Collectors.toMap(
                        d -> d.getCampoCrf().getIdCampo(),
                        d -> d));

        if (form.getDatosCrfList() != null) {
            for (DatosCrf datoForm : form.getDatosCrfList()) {

                if (datoForm.getCampoCrf() == null || datoForm.getCampoCrf().getIdCampo() == null) {
                    continue;
                }

                int idCampo = datoForm.getCampoCrf().getIdCampo();
                String valorForm = datoForm.getValor();

                String valorFinal = null;
                if (valorForm != null && !valorForm.trim().isEmpty()) {
                    valorFinal = valorForm.trim();
                }

                if (datosExistentesMap.containsKey(idCampo)) {

                    DatosCrf datoExistente = datosExistentesMap.get(idCampo);
                    datoExistente.setValor(valorFinal);
                } else {

                    if (valorFinal != null) {
                        CampoCrf campoReal = camposCRFRepository.findById(idCampo).orElse(null);
                        if (campoReal != null) {
                            DatosCrf nuevoDato = new DatosCrf();
                            nuevoDato.setCampoCrf(campoReal);
                            nuevoDato.setValor(valorFinal);
                            crf.addDato(nuevoDato);
                        }
                    }
                }
            }
        }

        crfRepository.save(crf);
        registroService.logCrfActivity("ACTUALIZACION_CRF", crf);
    }

    private void procesarYAdjuntarRespuestas(List<DatosCrf> respuestasDelForm, Crf crf) {
        if (respuestasDelForm == null)
            return;

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

            if ("SI/NO".equals(campoReal.getTipo()) || "SELECCION_UNICA".equals(campoReal.getTipo())) {
                if (valorOriginal != null) {
                    valorFinal = valorOriginal;
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
    public List<String> getMissingFields(Integer crfId) {
        Crf crf = crfRepository.findById(crfId)
                .orElseThrow(() -> new RuntimeException("CRF no encontrado con ID: " + crfId));
        Hibernate.initialize(crf.getDatosCrfList());

        // USO DEL NUEVO MÉTODO ORDENADO
        List<CampoCrf> allActiveFields = camposCRFRepository.findByActivoTrueOrderBySeccionAndNombre();

        Set<Integer> savedFieldIds = crf.getDatosCrfList().stream()
                .filter(dato -> dato.getCampoCrf() != null && dato.getValor() != null
                        && !dato.getValor().trim().isEmpty())
                .map(dato -> dato.getCampoCrf().getIdCampo())
                .collect(Collectors.toSet());

        List<String> missingFieldNames = allActiveFields.stream()
                .filter(campo -> !savedFieldIds.contains(campo.getIdCampo()))
                .map(CampoCrf::getNombre)
                .collect(Collectors.toList());

        return missingFieldNames;
    }

    @Transactional(readOnly = true)
    public CrfDetalleDTO getDetalleCrf(Integer id) {
        Crf crf = crfRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CRF no encontrado"));

        Hibernate.initialize(crf.getDatosPaciente());
        Hibernate.initialize(crf.getDatosCrfList());

        CrfDetalleDTO dto = new CrfDetalleDTO();
        dto.setIdCrf(crf.getIdCrf());
        dto.setFechaConsulta(crf.getFechaConsulta());
        dto.setEsCasoEstudio(crf.isCasoEstudio());
        dto.setObservacion(crf.getObservacion());

        if (crf.getDatosPaciente() != null) {
            dto.setNombrePaciente(crf.getDatosPaciente().getNombre() + " " + crf.getDatosPaciente().getApellido());
            dto.setCodigoPaciente(crf.getDatosPaciente().getCodigoPaciente());
        }

        List<CrfDetalleDTO.CampoValorDTO> listaCampos = new ArrayList<>();
        List<CampoCrf> camposActivos = camposCRFRepository.findByActivoTrueOrderBySeccionAndNombre();
        
        Map<Integer, String> respuestasMap = new HashMap<>();
        for (DatosCrf dato : crf.getDatosCrfList()) {
            if (dato.getCampoCrf() != null) {
                respuestasMap.put(dato.getCampoCrf().getIdCampo(), dato.getValor());
            }
        }

        for (CampoCrf campo : camposActivos) {
            String valRaw = respuestasMap.getOrDefault(campo.getIdCampo(), "-");
            String valFinal = valRaw;

            if(!"-".equals(valRaw)) {
                 if("SI/NO".equals(campo.getTipo())) {
                     valFinal = "1".equals(valRaw) ? "SÍ" : ("0".equals(valRaw) ? "NO" : valRaw);
                 } else if("SELECCION_UNICA".equals(campo.getTipo())) {
                     for(OpcionCampoCrf op : campo.getOpciones()) {
                         if(String.valueOf(op.getOrden()).equals(valRaw)) {
                             valFinal = op.getEtiqueta();
                             break;
                         }
                     }
                 }
            }

            String etiquetaMostrar = (campo.getPreguntaFormulario() != null && !campo.getPreguntaFormulario().isEmpty()) 
                                        ? campo.getPreguntaFormulario() 
                                        : campo.getNombre();

            listaCampos.add(new CrfDetalleDTO.CampoValorDTO(etiquetaMostrar, valFinal, campo.getSeccion()));
        }
        
        dto.setCampos(listaCampos);
        return dto;
    }
}