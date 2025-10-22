package cl.ubiobio.silkcorp.polygen_research;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("/inicio") // <-- CAMBIA ESTO
    public String mostrarInicio() {

        return "inicio";
    }
}