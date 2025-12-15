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

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import cl.ubiobio.silkcorp.polygen_research.security.CustomUserDetails;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistRepository;

@Controller
public class MainController {

    private final DashboardService dashboardService;
    private final NotaService notaService;
    private final FaqService faqService;
    private final WhitelistRepository whitelistRepository;

    public MainController(DashboardService dashboardService, 
                          NotaService notaService, 
                          FaqService faqService,
                          WhitelistRepository whitelistRepository) { // Agregado al constructor
        this.dashboardService = dashboardService;
        this.notaService = notaService;
        this.faqService = faqService;
        this.whitelistRepository = whitelistRepository;
    }

    @GetMapping("/inicio") 
    public String mostrarInicio(Model model) { 
        model.addAllAttributes(dashboardService.getDashboardStats());
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

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }
}