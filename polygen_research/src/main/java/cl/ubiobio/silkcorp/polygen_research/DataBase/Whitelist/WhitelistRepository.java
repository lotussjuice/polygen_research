package cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist;

import org.springframework.data.jpa.repository.EntityGraph; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; 
import java.util.Optional;

@Repository
public interface WhitelistRepository extends JpaRepository<Whitelist, Integer> {

    // Busca por correo, trayendo el usuario asociado
    @EntityGraph(attributePaths = {"usuario", "usuario.rolUsuario"}) // Carga el usuario y su rol
    Optional<Whitelist> findByCorreo(String correo);

    // Sobrescribe findAll para traer siempre el usuario asociado
    @Override
    @EntityGraph(attributePaths = {"usuario", "usuario.rolUsuario"}) // Carga el usuario y su rol
    List<Whitelist> findAll();
}
