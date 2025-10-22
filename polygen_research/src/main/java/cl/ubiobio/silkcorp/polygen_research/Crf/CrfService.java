package cl.ubiobio.silkcorp.polygen_research.Crf;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CrfService {

    private final CrfRepository crfRepository;

    public CrfService(CrfRepository crfRepository) {
        this.crfRepository = crfRepository;
    }

    public List<Crf> getAllCrfs() {
        return crfRepository.findAll();
    }

    public Crf saveCrf(Crf crf) {
        // logica correspondiente
        return crfRepository.save(crf);
    }
    
    // ... otros métodos CRUD (findById, deleteById, etc.)
}