package cl.ubiobio.silkcorp.polygen_research.auth;

import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuarioService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.UsuarioService;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistService;
import cl.ubiobio.silkcorp.polygen_research.security.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Random;
import java.util.Optional;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final WhitelistService whitelistService;
    private final RolUsuarioService rolUsuarioService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthController(UsuarioService usuarioService, WhitelistService whitelistService,
                          RolUsuarioService rolUsuarioService, PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.usuarioService = usuarioService;
        this.whitelistService = whitelistService;
        this.rolUsuarioService = rolUsuarioService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/login")
    public String showLoginPage(Model model, 
                                @RequestParam(value = "error", required = false) String error, 
                                HttpServletRequest request) {
        
        if (error != null) {
            String mensajeError = "Credenciales incorrectas."; // Mensaje por defecto
            HttpSession session = request.getSession(false);
            if (session != null) {
                Exception ex = (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
                
                if (ex instanceof DisabledException) {
                    mensajeError = "No se puede acceder: Esta cuenta fue eliminada.";
                }
            }

            model.addAttribute("errorMessage", mensajeError);
        }
        return "startpoint/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "startpoint/register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("registerDto") RegisterDto registerDto,
                                      BindingResult result,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {

        // 1. Validar campos obligatorios
        if (registerDto.getCorreo() == null || registerDto.getCorreo().isEmpty() ||
            registerDto.getPassword() == null || registerDto.getPassword().isEmpty() ||
            registerDto.getNombreUsuario() == null || registerDto.getNombreUsuario().isEmpty()) {
            result.reject("globalError", "Todos los campos son obligatorios.");
        }

        // 2. Validar que las contraseñas coincidan (NUEVO)
        if (registerDto.getPassword() != null && !registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.registerDto", "Las contraseñas no coinciden.");
        }

        // 3. Verificar duplicados en BD
        if (whitelistService.existsByCorreo(registerDto.getCorreo())) {
             result.rejectValue("correo", "error.correo", "El correo electrónico ya está registrado.");
        }

        Optional<Whitelist> credencialExistente = whitelistService.findByCorreo(registerDto.getCorreo());

        if (credencialExistente.isPresent()) {
            Usuario usuarioAsociado = credencialExistente.get().getUsuario();
            
            if (usuarioAsociado != null && "ELIMINADO".equals(usuarioAsociado.getEstado())) {
                result.rejectValue("correo", "error.deleted", 
                    "Esta cuenta fue eliminada. Contacte al administrador para reactivarla.");
                
                // Opcional: Si quieres un Toast flotante ADEMÁS del error en el input:
                redirectAttributes.addFlashAttribute("errorMessage", "Cuenta previamente eliminada. Correo sin acceso al sistema.");
                return "redirect:/register"; 
            } else {
                result.rejectValue("correo", "error.correo", "El correo electrónico ya está registrado.");
            }
        }

        if (result.hasErrors()) {
            return "startpoint/register";
        }

        try {
            // 4. Generar código y guardar en sesión
            String codigoVerificacion = String.format("%05d", new Random().nextInt(100000));
            session.setAttribute("tempUser", registerDto);
            session.setAttribute("verificationCode", codigoVerificacion);

            // 5. Enviar correo (CON MANEJO DE ERRORES)
            emailService.enviarCodigoVerificacion(registerDto.getCorreo(), registerDto.getNombreUsuario(), codigoVerificacion);

            return "redirect:/register/verify";

        } catch (Exception e) {
            // Si falla el correo (ej. no existe), volvemos al registro con el error
            // e.printStackTrace(); // Opcional para ver log
            // Usamos result.reject para que el error aparezca en el formulario, no como flash attribute
            result.reject("globalError", "No se pudo enviar el correo. Verifique que la dirección sea válida.");
            return "startpoint/register";
        }
    }

    @GetMapping("/register/verify")
    public String showVerificationPage(HttpSession session, Model model) {
        RegisterDto tempUser = (RegisterDto) session.getAttribute("tempUser");
        if (tempUser == null) {
            return "redirect:/register";
        }
        model.addAttribute("correo", tempUser.getCorreo());
        return "startpoint/verification";
    }

    @PostMapping("/register/verify")
    public String verifyCode(@RequestParam("codigo") String codigoIngresado,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        RegisterDto tempUser = (RegisterDto) session.getAttribute("tempUser");
        String realCode = (String) session.getAttribute("verificationCode");

        if (tempUser == null || realCode == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "La sesión ha expirado.");
            return "redirect:/register";
        }

        if (!realCode.equals(codigoIngresado.trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Código incorrecto.");
            return "redirect:/register/verify"; 
        }

        try {
            // Guardar usuario real
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombreUsuario(tempUser.getNombreUsuario());
            nuevoUsuario.setEstado("Activo");
            
            RolUsuario rolVisitante = rolUsuarioService.getRolVisitantePorDefecto();
            nuevoUsuario.setRolUsuario(rolVisitante);
            
            Usuario usuarioGuardado = usuarioService.saveUsuario(nuevoUsuario);

            Whitelist credencial = new Whitelist();
            credencial.setCorreo(tempUser.getCorreo());
            credencial.setContrasena(passwordEncoder.encode(tempUser.getPassword()));
            credencial.setUsuario(usuarioGuardado);

            whitelistService.saveCredencial(credencial);

            session.removeAttribute("tempUser");
            session.removeAttribute("verificationCode");

            redirectAttributes.addFlashAttribute("successMessage", "¡Cuenta creada! Inicie sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar en base de datos.");
            return "redirect:/register";
        }
    }

    @PostMapping("/register/resend")
    public String resendCode(HttpSession session, RedirectAttributes redirectAttributes) {
        RegisterDto tempUser = (RegisterDto) session.getAttribute("tempUser");
        
        if (tempUser == null) {
            return "redirect:/register";
        }

        String nuevoCodigo = String.format("%05d", new Random().nextInt(100000));
        session.setAttribute("verificationCode", nuevoCodigo);

        try {
            emailService.enviarCodigoVerificacion(tempUser.getCorreo(), tempUser.getNombreUsuario(), nuevoCodigo);
            redirectAttributes.addFlashAttribute("successMessage", "Nuevo código enviado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al reenviar. Verifique el correo.");
        }
        
        return "redirect:/register/verify";
    }

    @PostMapping("/register/cancel")
    public String cancelRegistration(HttpSession session) {
        session.removeAttribute("tempUser");
        session.removeAttribute("verificationCode");
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "startpoint/forgot_password";
    }

    // 2. Procesar correo y enviar código
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("correo") String correo,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        
        // Validar si el correo existe en el sistema
        Optional<Whitelist> credencialOpt = whitelistService.findByCorreo(correo);

        if (credencialOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "El correo no se encuentra registrado.");
            return "redirect:/forgot-password";
        }

        Usuario usuario = credencialOpt.get().getUsuario();
        if (usuario != null && "ELIMINADO".equals(usuario.getEstado())) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "No se puede recuperar la contraseña. Esta cuenta ha sido eliminada.");
            return "redirect:/forgot-password";
        }

        try {
            String codigoRecuperacion = String.format("%05d", new Random().nextInt(100000));
            
            // Guardar en sesión el correo y el código
            session.setAttribute("resetEmail", correo);
            session.setAttribute("resetCode", codigoRecuperacion);

            emailService.enviarCodigoRecuperacion(correo, codigoRecuperacion);
            
            return "redirect:/reset-password";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al enviar correo.");
            return "redirect:/forgot-password";
        }
    }

    // 3. Mostrar pantalla de cambio de contraseña
    @GetMapping("/reset-password")
    public String showResetPasswordPage(HttpSession session) {
        if (session.getAttribute("resetEmail") == null) {
            return "redirect:/forgot-password";
        }
        return "startpoint/reset_password";
    }

    // 4. Procesar el cambio de contraseña
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("codigo") String codigo,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {

        String sessionEmail = (String) session.getAttribute("resetEmail");
        String sessionCode = (String) session.getAttribute("resetCode");

        if (sessionEmail == null || sessionCode == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "La sesión expiró. Inicie el proceso nuevamente.");
            return "redirect:/forgot-password";
        }

        // Validar código
        if (!sessionCode.equals(codigo.trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Código incorrecto.");
            return "redirect:/reset-password";
        }

        // Validar contraseñas
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Las contraseñas no coinciden.");
            return "redirect:/reset-password";
        }

        try {
            // Buscar credencial
            Whitelist credencial = whitelistService.findByCorreo(sessionEmail)
                    .orElseThrow(() -> new Exception("Usuario no encontrado."));

            // Encriptar la contraseña ANTES de setearla en el objeto
            String passwordEncriptada = passwordEncoder.encode(newPassword);
            credencial.setContrasena(passwordEncriptada);
            // -----------------------
            
            // Guardar 
            whitelistService.saveCredencial(credencial); 

            // Limpiar sesión
            session.removeAttribute("resetEmail");
            session.removeAttribute("resetCode");

            redirectAttributes.addFlashAttribute("successMessage", "Contraseña restablecida. Inicie sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            e.printStackTrace(); // Útil para ver errores en consola
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar contraseña.");
            return "redirect:/reset-password";
        }
    }
}