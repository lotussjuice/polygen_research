package cl.ubiobio.silkcorp.polygen_research;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String mostrarInicio() {
        // Esto buscará el archivo /resources/templates/inicio.html
        return "inicio";
    }
}