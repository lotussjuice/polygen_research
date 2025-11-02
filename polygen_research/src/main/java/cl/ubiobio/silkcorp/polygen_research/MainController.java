package cl.ubiobio.silkcorp.polygen_research;

import cl.ubiobio.silkcorp.polygen_research.dashboard.DashboardService; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private final DashboardService dashboardService; 

    public MainController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/inicio") 
    public String mostrarInicio(Model model) { 
        
        // Carga del dashboard
        model.addAllAttributes(dashboardService.getDashboardStats());
        
        return "inicio"; 
    }

    // Por defecto redirigir a login
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }
}