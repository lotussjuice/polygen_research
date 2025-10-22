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
        // Lógica de negocio:
        // Asegurar que el 'crf' y 'campoCrf' asociados no sean nulos
        // Validar el 'valor' según el 'tipo' del campo (ej. si es numérico)
        return datosCrfRepository.save(datoCrf);
    }

    public List<DatosCrf> getAllDatosCrf() {
        return datosCrfRepository.findAll();
    }
    
}