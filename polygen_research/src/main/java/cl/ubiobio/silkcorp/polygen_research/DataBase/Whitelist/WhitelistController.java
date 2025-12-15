package cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist;

import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuarioService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.UsuarioService;
import cl.ubiobio.silkcorp.polygen_research.security.CustomUserDetails; // Importar UserDetails personalizado

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Importar AuthenticationPrincipal
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList; // Importar ArrayList para listas mutables
import java.util.List;


@Controller
@RequestMapping("/whitelist") 
public class WhitelistController {

    private final WhitelistService whitelistService;
    private final UsuarioService usuarioService;
    private final RolUsuarioService rolUsuarioService; 

    public WhitelistController(WhitelistService whitelistService, UsuarioService usuarioService, RolUsuarioService rolUsuarioService) {
        this.whitelistService = whitelistService;
        this.usuarioService = usuarioService;
        this.rolUsuarioService = rolUsuarioService; 
    }

    // Método para mostrar lista de usuarios y sus credenciales
    @GetMapping("/list")
    public String listarCredenciales(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 1. Obtener listas mutables (para poder remover elementos)
        List<Whitelist> listaCredenciales = new ArrayList<>(whitelistService.getAllCredencialesConUsuario()); 
        List<RolUsuario> listaRoles = new ArrayList<>(rolUsuarioService.getAllRoles()); 
        
        // 2. Identificar si el usuario logueado es DEV
        boolean esDev = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DEV"));
        
        // 3. Obtener el ID del usuario actual para impedir auto-edición en la vista
        Integer currentUserId = whitelistService.findByCorreo(userDetails.getUsername())
                .map(w -> w.getUsuario().getIdUsuario())
                .orElse(-1);

        // 4. Lógica de Filtrado de Seguridad
        if (!esDev) {
            // Si NO es DEV, ocultar a los usuarios que son DEV
            listaCredenciales.removeIf(w -> 
                w.getUsuario() != null && 
                w.getUsuario().getRolUsuario() != null && 
                "DEV".equals(w.getUsuario().getRolUsuario().getNombreRol())
            );
            
            // Si NO es DEV, quitar el rol "DEV" de la lista de opciones (nadie puede ascender a DEV)
            listaRoles.removeIf(r -> "DEV".equals(r.getNombreRol()));
        }

        model.addAttribute("credenciales", listaCredenciales);
        model.addAttribute("todosLosRoles", listaRoles); 
        model.addAttribute("currentUserId", currentUserId); // Enviamos el ID al frontend

        return "dev/WhitelistTemp/whitelist-list"; 
    }

    // Para crear nuevo registro
    @GetMapping("/nuevo")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("credencial", new Whitelist());
        model.addAttribute("usuarios", usuarioService.getAllUsuarios()); 
        return "dev/WhitelistTemp/whitelist-form";
    }

    @PostMapping("/guardar")
    public String guardarCredencial(@ModelAttribute Whitelist credencial) {
        whitelistService.saveCredencial(credencial);
        return "redirect:/whitelist/list";
    }


    @PutMapping("/usuario/{usuarioId}/rol")
    @ResponseBody 
    public ResponseEntity<String> actualizarRolUsuario(@PathVariable Integer usuarioId, @RequestParam Integer rolId) {
        try {
            usuarioService.updateUserRole(usuarioId, rolId);
            return ResponseEntity.ok("Rol actualizado correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al actualizar el rol: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error inesperado al actualizar el rol.");
        }
    }

    @DeleteMapping("/usuario/{usuarioId}")
    @ResponseBody
    public ResponseEntity<String> eliminarUsuario(@PathVariable Integer usuarioId) {
        try {
            usuarioService.softDeleteUsuario(usuarioId);
            return ResponseEntity.ok("Usuario eliminado correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al eliminar el usuario: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error inesperado al eliminar el usuario.");
        }
    }
}