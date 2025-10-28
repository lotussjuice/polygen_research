package cl.ubiobio.silkcorp.polygen_research;

import cl.ubiobio.silkcorp.polygen_research.dashboard.DashboardService; // 1. IMPORTAR SERVICIO
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // 2. IMPORTAR MODEL
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private final DashboardService dashboardService; // 3. INYECTAR SERVICIO

    public MainController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/inicio") 
    public String mostrarInicio(Model model) { // 4. AÑADIR MODEL
        
        // 5. OBTENER DATOS DEL BACKEND
        model.addAllAttributes(dashboardService.getDashboardStats());
        
        // 6. DEVOLVER LA VISTA
        return "inicio"; // Esto busca /templates/inicio.html
    }

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }
}