package cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist;

import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuario; // Importar RolUsuario
import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuarioService; // Importar RolUsuarioService
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.UsuarioService;
import org.springframework.http.ResponseEntity; // Importar ResponseEntity
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/whitelist") 
public class WhitelistController {

    private final WhitelistService whitelistService;
    private final UsuarioService usuarioService;
    private final RolUsuarioService rolUsuarioService; 

    public WhitelistController(WhitelistService whitelistService, UsuarioService usuarioService, RolUsuarioService rolUsuarioService) { // <-- AÑADIR al constructor
        this.whitelistService = whitelistService;
        this.usuarioService = usuarioService;
        this.rolUsuarioService = rolUsuarioService; 
    }

    // Método para mostrar lista de usuarios y sus credenciales
    @GetMapping("/list")
    public String listarCredenciales(Model model) {
        List<Whitelist> listaCredenciales = whitelistService.getAllCredencialesConUsuario(); 
        List<RolUsuario> listaRoles = rolUsuarioService.getAllRoles(); 
        model.addAttribute("credenciales", listaCredenciales);
        model.addAttribute("todosLosRoles", listaRoles); 
        return "Dev/WhitelistTemp/whitelist-list"; 
    }

    // Para crear nuevo registro, placeholder dev
    @GetMapping("/nuevo")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("credencial", new Whitelist());
        model.addAttribute("usuarios", usuarioService.getAllUsuarios()); // Usuarios sin credencial? Podría necesitar lógica
        return "Dev/WhitelistTemp/whitelist-form";
    }

    @PostMapping("/guardar")
    public String guardarCredencial(@ModelAttribute Whitelist credencial) {
        whitelistService.saveCredencial(credencial);
        return "redirect:/whitelist/list";
    }


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
            return ResponseEntity.internalServerError().body("Error inesperado al eliminar el usuario.");
        }
    }
}
