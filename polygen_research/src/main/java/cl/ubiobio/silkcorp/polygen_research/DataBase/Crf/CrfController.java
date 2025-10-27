package cl.ubiobio.silkcorp.polygen_research.DataBase.Crf; // O el paquete que corresponda

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfForm;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.CrfResumenViewDTO;


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

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarCrf(@PathVariable("id") Integer crfId, Model model) {
        
        // Llama al nuevo método del servicio
        CrfForm form = crfService.prepararCrfFormParaEditar(crfId);
        
        model.addAttribute("crfForm", form);
        
        // Reutiliza la misma vista del formulario
        return "dev/CrfTemp/Crf-form"; 
    }

    /**
     * Recibe los datos del formulario (paciente + respuestas dinámicas).
     * El @ModelAttribute "crfForm" une todos los campos del HTML
     * con el DTO CrfForm.
     */
    @PostMapping("/guardar")
    public String guardarNuevoCrf(@ModelAttribute CrfForm crfForm, Model model) {
        
        try {
            // --- ¡NUEVA LÓGICA DE DECISIÓN! ---
            if (crfForm.getIdCrf() == null) {
                // Si NO hay ID, es un CRF Nuevo
                crfService.guardarCrfCompleto(crfForm);
            } else {
                // Si SÍ hay ID, es una Edición
                crfService.actualizarCrfCompleto(crfForm);
            }

        } catch (Exception e) {
            // Manejo básico de errores (devuelve al usuario al formulario)
            model.addAttribute("errorMessage", "Error al guardar el CRF: " + e.getMessage());
            // Recarga el formulario con los datos que el usuario ya tenía
            model.addAttribute("crfForm", crfForm); 
            return "dev/CrfTemp/Crf-form";
        }

        // Redirige a la lista de CRFs
        return "redirect:/crf/list"; 
    }


    @GetMapping("/api/crf/{id}") // Nueva ruta para la API
    @ResponseBody
    public ResponseEntity<Crf> getCrfByIdApi(@PathVariable Integer id) {
        // Asumiendo que tienes un método getCrfById en tu CrfService
        return crfService.getCrfById(id) // Necesitas crear este método en CrfService si no existe
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Muestra el reporte de CRFs en formato de tabla pivotada.
     */
    @GetMapping("/reporte")
    public String mostrarReporteCrf(Model model) {
        
        // 1. Llama al nuevo método del servicio
        CrfResumenViewDTO data = crfService.getCrfResumenView();
        
        // 2. Pasamos las dos partes a la vista
        model.addAttribute("camposColumnas", data.getCamposActivos()); // Los <th>
        model.addAttribute("filasCrf", data.getFilas());         // Los <tr>
        
        // 3. El nombre de tu nuevo archivo HTML
        return "crf-reporte";
    }

    
    @GetMapping("/list")
        public String listarTodosLosCrfs(Model model) {
            
            // 1. Llama al servicio que ya creamos (getCrfResumenView)
            // Este servicio hace todo el trabajo de "pivotar" los datos.
            CrfResumenViewDTO data = crfService.getCrfResumenView();
            
            // 2. Pasamos las dos variables que el HTML necesita
            model.addAttribute("camposColumnas", data.getCamposActivos());
            model.addAttribute("filasCrf", data.getFilas());
            
            
            return "dev/CrfTemp/Crf-list"; 
        }

}
