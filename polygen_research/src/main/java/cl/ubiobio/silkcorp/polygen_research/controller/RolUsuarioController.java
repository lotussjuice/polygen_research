package cl.ubiobio.silkcorp.polygen_research.controller;

import cl.ubiobio.silkcorp.polygen_research.entity.RolUsuario;
import cl.ubiobio.silkcorp.polygen_research.service.RolUsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RolUsuarioController {

    private final RolUsuarioService rolUsuarioService;

    public RolUsuarioController(RolUsuarioService rolUsuarioService) {
        this.rolUsuarioService = rolUsuarioService;
    }

    @GetMapping
    public List<RolUsuario> getAllRoles() {
        return rolUsuarioService.getAllRoles();
    }

    @PostMapping
    public RolUsuario createRol(@RequestBody RolUsuario rol) {
        return rolUsuarioService.saveRol(rol);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RolUsuario> getRolById(@PathVariable Integer id) {
        return rolUsuarioService.getRolById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}