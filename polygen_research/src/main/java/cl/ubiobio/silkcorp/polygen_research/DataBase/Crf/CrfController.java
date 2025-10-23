package cl.ubiobio.silkcorp.polygen_research.DataBase.Crf; // O el paquete que corresponda

import org.springframework.http.ResponseEntity;

//import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfForm;


@Controller
@RequestMapping("/crf") // Ruta base para este controlador
public class CrfController {

    private final CrfService crfService;

    public CrfController(CrfService crfService) {
        this.crfService = crfService;
    }

    /**
     * Muestra el formulario de CRF dinámico.
     * Llama al servicio para preparar el DTO (CrfForm)
     * con los campos de paciente y la lista de campos dinámicos.
     */
    @GetMapping("/nuevo")
    public String mostrarNuevoCrfForm(Model model) {
        // Prepara el DTO (con paciente vacío y lista de respuestas vacías
        // pero asociadas a sus 'CamposCRF'
        CrfForm form = crfService.prepararNuevoCrfForm();
        
        model.addAttribute("crfForm", form);
        return "dev/CrfTemp/crf-form"; // El nombre de tu archivo HTML de Thymeleaf
    }

    /**
     * Recibe los datos del formulario (paciente + respuestas dinámicas).
     * El @ModelAttribute "crfForm" une todos los campos del HTML
     * con el DTO CrfForm.
     */
    @PostMapping("/guardar")
    public String guardarNuevoCrf(@ModelAttribute CrfForm crfForm, Model model) {
        
        try {
            // Llama al servicio para guardar todo en una transacción
            crfService.guardarCrfCompleto(crfForm);
        } catch (Exception e) {
            // Manejo básico de errores
            model.addAttribute("errorMessage", "Error al guardar el CRF: " + e.getMessage());
            // Devolvemos el formulario con los datos cargados para que el usuario corrija
            model.addAttribute("crfForm", crfForm);
            return "crf-form";
        }

        // Redirige a la lista de pacientes (o a donde quieras)
        return "redirect:/pacientes/list"; 
    }


    @GetMapping("/api/crf/{id}") // Nueva ruta para la API
    @ResponseBody
    public ResponseEntity<Crf> getCrfByIdApi(@PathVariable Integer id) {
        // Asumiendo que tienes un método getCrfById en tu CrfService
        return crfService.getCrfById(id) // Necesitas crear este método en CrfService si no existe
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /*
    @GetMapping("/list")
    public String listarTodosLosCrfs(Model model) {
        // 1. Llama al servicio para obtener todos los CRFs
        // (Este método ya debería existir en tu CrfService)
        List<Crf> listaDeCrfs = crfService.getAllCrfs();
        
        // 2. Agrega la lista al modelo para que la vista la pueda usar
        model.addAttribute("crfs", listaDeCrfs);
        
        // 3. Devuelve el nombre del nuevo archivo HTML que crearemos
        return "crf-list";
    }
    */
}
