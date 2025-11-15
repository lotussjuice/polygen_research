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
    public CampoCrf saveCampo(CampoCrf campoCrf) {
        if (campoCrf.getIdCampo() == null && campoCrf.getActivo() == null) { 
            campoCrf.setActivo(true);
        }

        if (!"SELECCION_UNICA".equals(campoCrf.getTipo())) {
            if (campoCrf.getOpciones() != null) {
                campoCrf.getOpciones().clear();
            }
        } else {
            if (campoCrf.getOpciones() != null) {
                int orden = 0;
                for (OpcionCampoCrf opcion : campoCrf.getOpciones()) {
                    opcion.setCampoCrf(campoCrf);
                    opcion.setOrden(orden++);
                }
            }
        }
        
        return campoCrfRepository.save(campoCrf);
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