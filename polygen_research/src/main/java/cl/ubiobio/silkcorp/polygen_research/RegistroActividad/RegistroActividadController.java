package cl.ubiobio.silkcorp.polygen_research.RegistroActividad;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/registros")
public class RegistroActividadController {

    private final RegistroActividadService registroService;

    public RegistroActividadController(RegistroActividadService registroService) {
        this.registroService = registroService;
    }

    @GetMapping("/list")
    public String listarRegistros(Model model) {
        List<RegistroActividad> listaRegistros = registroService.getAllRegistros();
        model.addAttribute("registros", listaRegistros);
        return "registro-list";
    }
}