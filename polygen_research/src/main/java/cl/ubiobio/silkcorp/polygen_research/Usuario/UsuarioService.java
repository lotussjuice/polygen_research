package cl.ubiobio.silkcorp.polygen_research.Usuario;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario saveUsuario(Usuario usuario) {
        // Lógica de negocio:
        // Ej: Asegurar que el rolUsuario no sea nulo.
        // Ej: Si se crea un usuario, poner 'Estado' en 'Activo'.
        return usuarioRepository.save(usuario);
    }
    
    // ... otros métodos CRUD
}