package cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 

import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuarioRepository; 

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolUsuarioRepository rolUsuarioRepository; 

    public UsuarioService(UsuarioRepository usuarioRepository, RolUsuarioRepository rolUsuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolUsuarioRepository = rolUsuarioRepository;
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario saveUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

  
    // Actualizar un rol (formulario en plataforma)
    @Transactional // Operación atómica
    public void updateUserRole(Integer usuarioId, Integer rolId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        RolUsuario nuevoRol = rolUsuarioRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + rolId));

        usuario.setRolUsuario(nuevoRol);
        usuarioRepository.save(usuario); // Guarda el cambio
    }


    // Elimina usuario y su credencial 
    @Transactional 
    public void deleteUsuario(Integer usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + usuarioId);
        }

        usuarioRepository.deleteById(usuarioId);
    }

 
    public Optional<Usuario> getUsuarioById(Integer id) {
        return usuarioRepository.findById(id);
    }
}
