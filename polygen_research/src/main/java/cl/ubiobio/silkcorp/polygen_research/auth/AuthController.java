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

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final WhitelistService whitelistService;
    private final RolUsuarioService rolUsuarioService;
    private final PasswordEncoder passwordEncoder;

    // Creación de objetos de para la instancia
    public AuthController(UsuarioService usuarioService, WhitelistService whitelistService,
                          RolUsuarioService rolUsuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.whitelistService = whitelistService;
        this.rolUsuarioService = rolUsuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "startpoint/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        // DTO para holdear la información momentaneamente
        model.addAttribute("registerDto", new RegisterDto());
        return "startpoint/register";
    }

    // Procesamiento de un registro
    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("registerDto") RegisterDto registerDto,
                                      BindingResult result, // Para validar resultados
                                      RedirectAttributes redirectAttributes) { // Para mensages

    
        if (registerDto.getCorreo() == null || registerDto.getCorreo().isEmpty() ||
            registerDto.getPassword() == null || registerDto.getPassword().isEmpty() ||
            registerDto.getNombreUsuario() == null || registerDto.getNombreUsuario().isEmpty()) {
            result.reject("globalError", "Todos los campos son obligatorios.");
        }

        // Chequea que correo no exista en el registro
        // Proximamente conexión con correos reales de ser posible
        if (whitelistService.existsByCorreo(registerDto.getCorreo())) {
             result.rejectValue("correo", "error.correo", "El correo electrónico ya está registrado.");
        }


        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerDto", result);
            redirectAttributes.addFlashAttribute("registerDto", registerDto); // Mantener input
            return "redirect:/register?error";
        }

        try {
            // Crear usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombreUsuario(registerDto.getNombreUsuario());
            nuevoUsuario.setEstado("Activo"); // Status default

            // Rol visitante por defecto
            RolUsuario rolVisitante = rolUsuarioService.getRolVisitantePorDefecto();
            nuevoUsuario.setRolUsuario(rolVisitante);
            
            Usuario usuarioGuardado = usuarioService.saveUsuario(nuevoUsuario); 

            // Guardar credenciales en whitelist
            Whitelist credencial = new Whitelist();
            credencial.setCorreo(registerDto.getCorreo());
            // Guardar contraseña hasheada
            credencial.setContrasena(passwordEncoder.encode(registerDto.getPassword()));
            credencial.setUsuario(usuarioGuardado); 

            whitelistService.saveCredencial(credencial);

        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar el usuario.");
             return "redirect:/register?error";
        }

        redirectAttributes.addFlashAttribute("successMessage", "¡Usuario registrado con éxito! Por favor, inicie sesión.");
        return "redirect:/login"; 
    }
}