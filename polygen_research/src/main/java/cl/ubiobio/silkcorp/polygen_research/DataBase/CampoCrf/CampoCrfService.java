package cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.ubiobio.silkcorp.polygen_research.DataBase.OpcionCampoCrf.OpcionCampoCrf;

@Service
public class CampoCrfService {

    private final CampoCrfRepository campoCrfRepository;

    public CampoCrfService(CampoCrfRepository campoCrfRepository) {
        this.campoCrfRepository = campoCrfRepository;
    }

    public List<CampoCrf> getAllCampos() {
        return campoCrfRepository.findAll();
    }

    @Transactional
    public CampoCrf saveCampo(CampoCrf campoForm) {
        if (campoForm.getIdCampo() == null) {
            if (campoForm.getActivo() == null) {
                campoForm.setActivo(true);
            }
            procesarOpciones(campoForm);
            return campoCrfRepository.save(campoForm);
        } 
        
        else {
            CampoCrf campoExistente = campoCrfRepository.findById(campoForm.getIdCampo())
                    .orElseThrow(() -> new RuntimeException("Campo no encontrado con ID: " + campoForm.getIdCampo()));

            campoExistente.setNombre(campoForm.getNombre());
            campoExistente.setPreguntaFormulario(campoForm.getPreguntaFormulario());
            campoExistente.setSeccion(campoForm.getSeccion());
            campoExistente.setDescripcion(campoForm.getDescripcion());


            String tipoAnterior = campoExistente.getTipo();
            String tipoNuevo = campoForm.getTipo();

            if (!tipoAnterior.equals(tipoNuevo)) {
                campoExistente.setTipo(tipoNuevo);
                if (campoExistente.getDatosCrfList() != null) {
                    campoExistente.getDatosCrfList().clear();
                }
            } 

            // Limpiar opciones anteriores y agregamos las nuevas del formulario
            if (campoExistente.getOpciones() != null) {
                campoExistente.getOpciones().clear();
            }
            
            if ("SELECCION_UNICA".equals(tipoNuevo) && campoForm.getOpciones() != null) {
                int orden = 0;
                for (OpcionCampoCrf opForm : campoForm.getOpciones()) {
                    if (opForm.getEtiqueta() != null && !opForm.getEtiqueta().trim().isEmpty()) {
                        OpcionCampoCrf nuevaOp = new OpcionCampoCrf();
                        nuevaOp.setEtiqueta(opForm.getEtiqueta());
                        nuevaOp.setOrden(orden++);
                        campoExistente.addOpcion(nuevaOp);
                    }
                }
            }

            return campoCrfRepository.save(campoExistente);
        }
    }

    // Método auxiliar para procesar opciones en la creación
    private void procesarOpciones(CampoCrf campo) {
        if (!"SELECCION_UNICA".equals(campo.getTipo())) {
            if (campo.getOpciones() != null) {
                campo.getOpciones().clear();
            }
        } else {
            if (campo.getOpciones() != null) {
                campo.getOpciones().removeIf(op -> 
                    op.getEtiqueta() == null || op.getEtiqueta().trim().isEmpty()
                );
                int orden = 0;
                for (OpcionCampoCrf opcion : campo.getOpciones()) {
                    opcion.setCampoCrf(campo);
                    opcion.setOrden(orden++);
                }
            }
        }
    }

    public Optional<CampoCrf> getCampoById(Integer id) {
        return campoCrfRepository.findByIdWithOptions(id);
    }

    @Transactional
    public void toggleEstadoCampo(Integer id) {
        CampoCrf campo = campoCrfRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campo no encontrado con ID: " + id));
        boolean estadoActual = (campo.getActivo() != null) ? campo.getActivo() : false;
        campo.setActivo(!estadoActual);
        campoCrfRepository.save(campo);
    }

}