package cl.ubiobio.silkcorp.polygen_research.controller;

import cl.ubiobio.silkcorp.polygen_research.entity.DatosPaciente;
import cl.ubiobio.silkcorp.polygen_research.service.DatosPacienteService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pacientes") // Define la URL base para este controlador
public class DatosPacienteController {

    private final DatosPacienteService pacienteService;

    public DatosPacienteController(DatosPacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public List<DatosPaciente> getAllPacientes() {
        return pacienteService.getAllPacientes();
    }

    @PostMapping
    public DatosPaciente createPaciente(@RequestBody DatosPaciente paciente) {
        return pacienteService.savePaciente(paciente);
    }

    // Puedes agregar más endpoints para GET por ID, DELETE, etc.
}