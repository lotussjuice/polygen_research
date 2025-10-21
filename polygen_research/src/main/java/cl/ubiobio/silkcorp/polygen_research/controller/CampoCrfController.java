package cl.ubiobio.silkcorp.polygen_research.controller;

import cl.ubiobio.silkcorp.polygen_research.entity.CampoCrf;
import cl.ubiobio.silkcorp.polygen_research.service.CampoCrfService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/campos-crf") // URL base para los campos
public class CampoCrfController {

    private final CampoCrfService campoCrfService;

    public CampoCrfController(CampoCrfService campoCrfService) {
        this.campoCrfService = campoCrfService;
    }

    @GetMapping
    public List<CampoCrf> getAllCampos() {
        return campoCrfService.getAllCampos();
    }

    @PostMapping
    public CampoCrf createCampo(@RequestBody CampoCrf campoCrf) {
        return campoCrfService.saveCampo(campoCrf);
    }
}