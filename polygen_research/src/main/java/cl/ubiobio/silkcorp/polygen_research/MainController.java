package cl.ubiobio.silkcorp.polygen_research;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import cl.ubiobio.silkcorp.polygen_research.dashboard.DashboardService;
import cl.ubiobio.silkcorp.polygen_research.faq.FaqService;
import cl.ubiobio.silkcorp.polygen_research.notes.Nota;
import cl.ubiobio.silkcorp.polygen_research.notes.NotaService;

@Controller
public class MainController {

    private final DashboardService dashboardService;
    private final NotaService notaService;
    private final FaqService faqService;

    public MainController(DashboardService dashboardService, NotaService notaService, FaqService faqService) {
        this.dashboardService = dashboardService;
        this.notaService = notaService;
        this.faqService = faqService;
    }

    @GetMapping("/inicio") 
    public String mostrarInicio(Model model) { 
        
        // Carga del dashboard
        model.addAllAttributes(dashboardService.getDashboardStats());

        //Carga de notas
        model.addAttribute("listaNotas", notaService.listarTodas());
        
        return "inicio"; 
    }

    @PostMapping("/notas/guardar")
    public String guardarNota(@ModelAttribute Nota nota) {
        notaService.guardar(nota);
        return "redirect:/inicio";
    }

    @GetMapping("/notas/eliminar/{id}")
    public String eliminarNota(@PathVariable Long id) {
        notaService.eliminar(id);
        return "redirect:/inicio";
    }


    @GetMapping("/faq")
    public String mostrarFaq(Model model) {
        model.addAttribute("listaFaqs", faqService.obtenerPreguntas());
        return "faq/faq";
    }



    // Por defecto redirigir a login
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

}