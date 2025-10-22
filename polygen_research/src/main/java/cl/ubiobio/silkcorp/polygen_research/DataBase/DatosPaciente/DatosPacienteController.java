package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/pacientes")
public class DatosPacienteController {

    private final DatosPacienteService pacienteService;

    public DatosPacienteController(DatosPacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping("/list")
    public String listarPacientes(Model model) {
        List<DatosPaciente> listaPacientes = pacienteService.getAllPacientes();
        model.addAttribute("pacientes", listaPacientes);
        return "Dev/DatosPacienteTemp/paciente-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDeNuevoPaciente(Model model) {
        model.addAttribute("paciente", new DatosPaciente());
        return "Dev/DatosPacienteTemp/paciente-form";
    }

    @PostMapping("/guardar")
    public String guardarPaciente(@ModelAttribute DatosPaciente paciente) {
        pacienteService.savePaciente(paciente);
        return "redirect:/pacientes/list";
    }
}