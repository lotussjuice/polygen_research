package cl.ubiobio.silkcorp.polygen_research.auth;

import java.util.Random;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.UsuarioRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistRepository;
import cl.ubiobio.silkcorp.polygen_research.security.CustomUserDetails;
import cl.ubiobio.silkcorp.polygen_research.security.EmailService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final WhitelistRepository whitelistRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PerfilController(WhitelistRepository whitelistRepository, UsuarioRepository usuarioRepository, 
                            EmailService emailService, PasswordEncoder passwordEncoder) {
        this.whitelistRepository = whitelistRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. MOSTRAR PERFIL
    @GetMapping
    public String mostrarPerfil(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails.getUsername();
        Whitelist credencial = whitelistRepository.findByCorreo(email).orElseThrow();
        Usuario usuario = credencial.getUsuario();

        model.addAttribute("usuario", usuario);
        model.addAttribute("correoReal", email);
        model.addAttribute("correoEnmascarado", enmascararCorreo(email));

        return "perfil";
    }

    // 2. ACTUALIZAR NOMBRE DE USUARIO
    @PostMapping("/actualizar-nombre")
    public String actualizarNombre(@RequestParam("nuevoNombre") String nuevoNombre, 
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails.getUsername();
        Whitelist credencial = whitelistRepository.findByCorreo(email).orElseThrow();
        Usuario usuario = credencial.getUsuario();

        usuario.setNombreUsuario(nuevoNombre);
        usuarioRepository.save(usuario);
        
        return "redirect:/perfil?successName=true";
    }

    @PostMapping("/api/enviar-codigo")
    @ResponseBody
    public ResponseEntity<String> enviarCodigoSeguridad(@AuthenticationPrincipal CustomUserDetails userDetails, HttpSession session) {
        String email = userDetails.getUsername();
        
        String codigo = String.format("%06d", new Random().nextInt(999999));
        
        session.setAttribute("codigo_seguridad_perfil", codigo);
        session.setAttribute("codigo_timestamp", System.currentTimeMillis());

        emailService.enviarCodigoRecuperacion(email, codigo); 

        return ResponseEntity.ok("Código enviado");
    }

    // 4. API: VALIDAR CÓDIGO
    @PostMapping("/api/validar-codigo")
    @ResponseBody
    public ResponseEntity<String> validarCodigo(@RequestParam("codigo") String codigoInput, HttpSession session) {
        String codigoReal = (String) session.getAttribute("codigo_seguridad_perfil");
        
        if (codigoReal != null && codigoReal.equals(codigoInput)) {
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.badRequest().body("Código incorrecto");
        }
    }

    // 5. API: CAMBIAR CONTRASEÑA
    @PostMapping("/api/cambiar-password")
    @ResponseBody
    public ResponseEntity<String> cambiarPassword(@RequestParam("nuevaPass") String nuevaPass, 
                                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                                  HttpSession session) {

        String email = userDetails.getUsername();
        Whitelist credencial = whitelistRepository.findByCorreo(email).orElseThrow();

        credencial.setContrasena(passwordEncoder.encode(nuevaPass));
        whitelistRepository.save(credencial);
        
        session.removeAttribute("codigo_seguridad_perfil");

        return ResponseEntity.ok("Contraseña actualizada");
    }

    private String enmascararCorreo(String email) {
        if (email == null || !email.contains("@")) return email;
        
        String[] partes = email.split("@");
        String nombre = partes[0];
        String dominio = partes[1];

        if (nombre.length() <= 3) {
            return nombre.substring(0, 1) + "***@" + dominio;
        }
        
        return nombre.substring(0, 3) + "***@" + dominio;
    }
}