package cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist;

import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuario; // Importar RolUsuario
import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuarioService; // Importar RolUsuarioService
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.UsuarioService;
import org.springframework.http.ResponseEntity; // Importar ResponseEntity
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Para mensajes flash

import java.util.List;


@Controller
@RequestMapping("/whitelist") // Ya restringido a ADMIN por SecurityConfig
public class WhitelistController {

    private final WhitelistService whitelistService;
    private final UsuarioService usuarioService;
    private final RolUsuarioService rolUsuarioService; // <-- AÑADIR RolUsuarioService

    public WhitelistController(WhitelistService whitelistService, UsuarioService usuarioService, RolUsuarioService rolUsuarioService) { // <-- AÑADIR al constructor
        this.whitelistService = whitelistService;
        this.usuarioService = usuarioService;
        this.rolUsuarioService = rolUsuarioService; // <-- AÑADIR
    }

    @GetMapping("/list")
    public String listarCredenciales(Model model) {
        List<Whitelist> listaCredenciales = whitelistService.getAllCredencialesConUsuario(); // Usar método que cargue Usuario EAGER
        List<RolUsuario> listaRoles = rolUsuarioService.getAllRoles(); // <-- OBTENER ROLES
        model.addAttribute("credenciales", listaCredenciales);
        model.addAttribute("todosLosRoles", listaRoles); // <-- PASAR ROLES A LA VISTA
        return "Dev/WhitelistTemp/whitelist-list"; // Asegúrate que esta es la ruta correcta
    }

    // Los métodos /nuevo y /guardar para Whitelist pueden quedarse si son necesarios
    // para crear credenciales manualmente, pero la gestión principal se hará en la lista.
    @GetMapping("/nuevo")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("credencial", new Whitelist());
        model.addAttribute("usuarios", usuarioService.getAllUsuarios()); // Usuarios sin credencial? Podría necesitar lógica
        return "Dev/WhitelistTemp/whitelist-form";
    }

    @PostMapping("/guardar")
    public String guardarCredencial(@ModelAttribute Whitelist credencial) {
        // Aquí deberías añadir lógica para encriptar contraseña si se crea/modifica
        whitelistService.saveCredencial(credencial);
        return "redirect:/whitelist/list";
    }

    // --- NUEVO ENDPOINT PARA ACTUALIZAR ROL ---
    // Usamos @PutMapping para la actualización, se llamará con AJAX
    @PutMapping("/usuario/{usuarioId}/rol")
    @ResponseBody // Indica que devolvemos datos (ej. un mensaje), no una vista
    public ResponseEntity<String> actualizarRolUsuario(@PathVariable Integer usuarioId, @RequestParam Integer rolId) {
        try {
            usuarioService.updateUserRole(usuarioId, rolId);
            return ResponseEntity.ok("Rol actualizado correctamente.");
        } catch (RuntimeException e) {
            // Loggear el error e.getMessage()
            return ResponseEntity.badRequest().body("Error al actualizar el rol: " + e.getMessage());
        } catch (Exception e) {
            // Loggear el error e.getMessage()
            return ResponseEntity.internalServerError().body("Error inesperado al actualizar el rol.");
        }
    }

    // --- NUEVO ENDPOINT PARA ELIMINAR USUARIO ---
    // Usamos @DeleteMapping, se llamará con AJAX
    @DeleteMapping("/usuario/{usuarioId}")
    @ResponseBody
    public ResponseEntity<String> eliminarUsuario(@PathVariable Integer usuarioId) {
        try {
            usuarioService.deleteUsuario(usuarioId);
            return ResponseEntity.ok("Usuario eliminado correctamente.");
        } catch (RuntimeException e) {
             // Loggear el error e.getMessage()
            return ResponseEntity.badRequest().body("Error al eliminar el usuario: " + e.getMessage());
        } catch (Exception e) {
             // Loggear el error e.getMessage()
             // Considerar DataIntegrityViolationException si hay referencias
            return ResponseEntity.internalServerError().body("Error inesperado al eliminar el usuario.");
        }
    }
}
