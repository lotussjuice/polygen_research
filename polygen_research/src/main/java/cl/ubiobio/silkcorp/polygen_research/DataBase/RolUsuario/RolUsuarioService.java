package cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RolUsuarioService {

    private final RolUsuarioRepository rolUsuarioRepository;

    public RolUsuarioService(RolUsuarioRepository rolUsuarioRepository) {
        this.rolUsuarioRepository = rolUsuarioRepository;
    }

    public List<RolUsuario> getAllRoles() {
        return rolUsuarioRepository.findAll();
    }

    public Optional<RolUsuario> getRolById(Integer id) {
        return rolUsuarioRepository.findById(id);
    }

    public RolUsuario saveRol(RolUsuario rol) {
        // Lógica de negocio (ej: validar que el nombre del rol no se repita)
        return rolUsuarioRepository.save(rol);
    }

    public void deleteRol(Integer id) {
        rolUsuarioRepository.deleteById(id);
    }
}