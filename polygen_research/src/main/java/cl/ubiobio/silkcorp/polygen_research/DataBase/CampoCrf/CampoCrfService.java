package cl.ubiobio.silkcorp.polygen_research.DataBase.CampoCrf;

import org.springframework.stereotype.Service;


import java.util.List;

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
        // Aquí iría lógica validacion
        return campoCrfRepository.save(campoCrf);
    }
    
    // ... otros métodos CRUD (findById, deleteById, etc.)
}