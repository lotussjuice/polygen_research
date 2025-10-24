package cl.ubiobio.silkcorp.polygen_research.DataBase.DatosPaciente;

import org.springframework.http.ResponseEntity;
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
        
        // --- ¡ARREGLO AQUÍ! ---
        // Apuntamos a la ruta correcta que tenías originalmente
        return "Dev/DatosPacienteTemp/paciente-list"; 
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDeNuevoPaciente(Model model) {
        model.addAttribute("paciente", new DatosPaciente());

        // --- ¡ARREGLO AQUÍ! ---
        // También corregimos esta ruta
        return "Dev/DatosPacienteTemp/paciente-form"; 
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEditar(@PathVariable Integer id, Model model) {
        DatosPaciente paciente = pacienteService.getPacienteById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de paciente inválido:" + id));
        
        model.addAttribute("paciente", paciente);

        // --- ¡ARREGLO AQUÍ! ---
        // Y también esta
        return "Dev/DatosPacienteTemp/paciente-form";
    }

    @PostMapping("/guardar")
    public String guardarPaciente(@ModelAttribute DatosPaciente paciente) {
        pacienteService.savePaciente(paciente);
        return "redirect:/pacientes/list";
    }

    @GetMapping("/api/paciente/{id}") 
    @ResponseBody 
    public ResponseEntity<DatosPaciente> getPacienteByIdApi(@PathVariable Integer id) {
        return pacienteService.getPacienteById(id)
                .map(ResponseEntity::ok) 
                .orElse(ResponseEntity.notFound().build()); 
    }
}