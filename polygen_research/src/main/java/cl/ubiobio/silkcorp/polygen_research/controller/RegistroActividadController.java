package cl.ubiobio.silkcorp.polygen_research.controller;

import cl.ubiobio.silkcorp.polygen_research.entity.RegistroActividad;
import cl.ubiobio.silkcorp.polygen_research.service.RegistroActividadService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registros-actividad")
public class RegistroActividadController {

    private final RegistroActividadService registroService;

    public RegistroActividadController(RegistroActividadService registroService) {
        this.registroService = registroService;
    }

    @PostMapping
    public RegistroActividad createRegistro(@RequestBody RegistroActividad registro) {
        return registroService.saveRegistro(registro);
    }
}