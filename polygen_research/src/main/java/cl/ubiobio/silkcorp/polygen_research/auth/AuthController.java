package cl.ubiobio.silkcorp.polygen_research.auth;

import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuarioService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.UsuarioService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; 

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 

import java.util.List; // Esta importación ya no es necesaria aquí, pero no daña

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final WhitelistService whitelistService;
    private final RolUsuarioService rolUsuarioService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioService usuarioService, WhitelistService whitelistService,
                          RolUsuarioService rolUsuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.whitelistService = whitelistService;
        this.rolUsuarioService = rolUsuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    // Show Login Page
    @GetMapping("/login")
    public String showLoginPage() {
        return "startpoint/login";
    }

    // Show Registration Page
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        // We need a DTO (Data Transfer Object) to hold registration data
        model.addAttribute("registerDto", new RegisterDto());
        
        // ==== CAMBIO AQUÍ ====
        // Ya no pasamos la lista de roles al formulario
        // List<RolUsuario> roles = rolUsuarioService.getAllRoles(); // <-- LÍNEA ELIMINADA
        // model.addAttribute("roles", roles); // <-- LÍNEA ELIMINADA
        // ==== FIN DEL CAMBIO ====
        
        // Corresponds to /resources/templates/register.html
        return "startpoint/register";
    }

    // Process Registration Form
    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("registerDto") RegisterDto registerDto,
                                      BindingResult result, // For validation results
                                      RedirectAttributes redirectAttributes) { // To show success/error messages

        // ==== CAMBIO EN VALIDACIÓN ====
        // Se quita la validación de 'registerDto.getIdRol()'
        if (registerDto.getCorreo() == null || registerDto.getCorreo().isEmpty() ||
            registerDto.getPassword() == null || registerDto.getPassword().isEmpty() ||
            registerDto.getNombreUsuario() == null || registerDto.getNombreUsuario().isEmpty()) {
            // Add an error message if validation fails
            result.reject("globalError", "Todos los campos son obligatorios.");
        }
        // ==== FIN DEL CAMBIO ====


        // Check if email already exists
        if (whitelistService.existsByCorreo(registerDto.getCorreo())) {
             result.rejectValue("correo", "error.correo", "El correo electrónico ya está registrado.");
        }


        if (result.hasErrors()) {
            // If there are errors, return to the registration form
            // Need to add roles back to the model for the dropdown
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerDto", result);
            redirectAttributes.addFlashAttribute("registerDto", registerDto); // Keep user input
            return "redirect:/register?error"; // Redirect to avoid form resubmission
        }

        // --- Registration Logic ---
        try {
            // 1. Create Usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombreUsuario(registerDto.getNombreUsuario());
            nuevoUsuario.setEstado("Activo"); // Set default state

            // ==== CAMBIO EN ASIGNACIÓN DE ROL ====
            // En lugar de obtener el rol del DTO, asignamos "VISITANTE" por defecto
            RolUsuario rolVisitante = rolUsuarioService.getRolVisitantePorDefecto();
            nuevoUsuario.setRolUsuario(rolVisitante);
            // ==== FIN DEL CAMBIO ====
            
            Usuario usuarioGuardado = usuarioService.saveUsuario(nuevoUsuario); // Save and get ID

            // 2. Create Whitelist (Credentials) - SIN CAMBIOS
            Whitelist credencial = new Whitelist();
            credencial.setCorreo(registerDto.getCorreo());
            // Encode the password before saving!
            credencial.setContrasena(passwordEncoder.encode(registerDto.getPassword()));
            credencial.setUsuario(usuarioGuardado); // Link to the saved Usuario

            whitelistService.saveCredencial(credencial);

        } catch (Exception e) {
             // Handle potential errors during save
             redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar el usuario.");
             return "redirect:/register?error";
        }

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "¡Usuario registrado con éxito! Por favor, inicie sesión.");
        return "redirect:/login"; // Redirect to login page after successful registration
    }
}