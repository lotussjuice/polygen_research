package cl.ubiobio.silkcorp.polygen_research.controller;

import cl.ubiobio.silkcorp.polygen_research.entity.Crf;
import cl.ubiobio.silkcorp.polygen_research.service.CrfService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/crfs") // URL base para los CRFs
public class CrfController {

    private final CrfService crfService;

    public CrfController(CrfService crfService) {
        this.crfService = crfService;
    }

    @GetMapping
    public List<Crf> getAllCrfs() {
        return crfService.getAllCrfs();
    }

    @PostMapping
    public Crf createCrf(@RequestBody Crf crf) {
        // Cuando envíes un JSON/Excel para crear un CRF, 
        // deberá incluir el ID del paciente al que pertenece.
        return crfService.saveCrf(crf);
    }
}