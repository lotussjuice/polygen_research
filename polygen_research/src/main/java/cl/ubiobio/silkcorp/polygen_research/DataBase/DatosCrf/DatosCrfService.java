package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosCrf;

import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class DatosCrfService {

    private final DatosCrfRepository datosCrfRepository;

    public DatosCrfService(DatosCrfRepository datosCrfRepository) {
        this.datosCrfRepository = datosCrfRepository;
    }

    public DatosCrf saveDatoCrf(DatosCrf datoCrf) {
        return datosCrfRepository.save(datoCrf);
    }

    public List<DatosCrf> getAllDatosCrf() {
        return datosCrfRepository.findAll();
    }
    
}