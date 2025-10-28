package cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class CampoCrfService {

    private final CampoCrfRepository campoCrfRepository;

    public CampoCrfService(CampoCrfRepository campoCrfRepository) {
        this.campoCrfRepository = campoCrfRepository;
    }

    public List<CampoCrf> getAllCampos() {
        return campoCrfRepository.findAll();
    }

    public CampoCrf saveCampo(CampoCrf campoCrf) {
        if (campoCrf.getIdCampo() == null && campoCrf.getActivo() == null) { 
            campoCrf.setActivo(true);
        }
        return campoCrfRepository.save(campoCrf);
    }

    public Optional<CampoCrf> getCampoById(Integer id) {
        return campoCrfRepository.findById(id);
    }

    public void toggleEstadoCampo(Integer id) {
        CampoCrf campo = getCampoById(id)
                .orElseThrow(() -> new RuntimeException("Campo no encontrado con ID: " + id));
        boolean estadoActual = (campo.getActivo() != null) ? campo.getActivo() : false;
        campo.setActivo(!estadoActual);
        campoCrfRepository.save(campo);
    }

}