package cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar Transactional

import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuarioRepository; // Importar Optional

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolUsuarioRepository rolUsuarioRepository; // Necesario para buscar el nuevo rol

    public UsuarioService(UsuarioRepository usuarioRepository, RolUsuarioRepository rolUsuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolUsuarioRepository = rolUsuarioRepository;
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

    // --- NUEVO MÉTODO ---
    /**
     * Actualiza únicamente el rol de un usuario existente.
     * @param usuarioId El ID del usuario a modificar.
     * @param rolId El ID del nuevo rol a asignar.
     * @throws RuntimeException si el usuario o el rol no se encuentran.
     */
    @Transactional // Asegura que la operación sea atómica
    public void updateUserRole(Integer usuarioId, Integer rolId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        RolUsuario nuevoRol = rolUsuarioRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + rolId));

        usuario.setRolUsuario(nuevoRol);
        usuarioRepository.save(usuario); // Guarda el cambio
    }


    /**
     * Elimina un usuario por su ID.
     * eliminar la credencial asociada.
     * @param usuarioId El ID del usuario a eliminar.
     */
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
