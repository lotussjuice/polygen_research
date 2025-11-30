package cl.ubiobio.silkcorp.polygen_research.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.RolUsuario.RolUsuarioRepository;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolUsuarioRepository rolUsuarioRepository;

    public DataInitializer(RolUsuarioRepository rolUsuarioRepository) {
        this.rolUsuarioRepository = rolUsuarioRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (rolUsuarioRepository.count() == 0) {
            System.out.println("Inicializando base de datos con Roles por defecto...");

            RolUsuario dev = new RolUsuario();
            dev.setNombreRol("DEV");

            RolUsuario investigador = new RolUsuario();
            investigador.setNombreRol("INVESTIGADOR");

            RolUsuario admin = new RolUsuario();
            admin.setNombreRol("ADMINISTRADOR");

            RolUsuario visitante = new RolUsuario();
            visitante.setNombreRol("VISITANTE");

            List<RolUsuario> roles = Arrays.asList(dev, investigador, admin, visitante);

            rolUsuarioRepository.saveAll(roles);

            System.out.println("Roles insertados correctamente.");
        } else {
            System.out.println("La base de datos ya contiene roles. Se omite la inicialización.");
        }
    }
}