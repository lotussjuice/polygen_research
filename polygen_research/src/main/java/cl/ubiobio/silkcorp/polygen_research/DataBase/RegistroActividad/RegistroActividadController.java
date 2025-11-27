package cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.RegistroActividadDTO;

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
        return "dev/RegistroActividadTemp/registro-list";
    }

    // En RegistroActividadController.java

    @GetMapping("/api/recent")
    @ResponseBody
    public ResponseEntity<List<RegistroActividadDTO>> getRecentActivity() {
        // Asumo que tienes un método en tu servicio para esto: registroService.getUltimosRegistros(10)
        // Debes crear un DTO simple para no enviar todo el objeto con relaciones cíclicas
        return ResponseEntity.ok(registroService.getUltimosRegistrosDTO(4));
    }
}