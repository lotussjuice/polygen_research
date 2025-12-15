package cl.ubiobio.silkcorp.polygen_research.DataBase.RegistroActividad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import cl.ubiobio.silkcorp.polygen_research.DataBase.Crf.Crf;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Usuario.Usuario;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.Whitelist;
import cl.ubiobio.silkcorp.polygen_research.DataBase.Whitelist.WhitelistRepository;
import cl.ubiobio.silkcorp.polygen_research.DataBase.dto.RegistroActividadDTO;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class RegistroActividadService {

    @Autowired
    private final RegistroActividadRepository registroRepository;

    @Autowired
    private WhitelistRepository whitelistRepository;

    public RegistroActividadService(RegistroActividadRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    public RegistroActividad saveRegistro(RegistroActividad registro) {
        return registroRepository.save(registro);
    }

    public List<RegistroActividad> getAllRegistros() {
        return registroRepository.findAll();
    }

    public void logCrfActivity(String tipoActividad, Crf crf) {
        try {
            // Obtener el email del usuario logueado
            String usernameEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            // Buscar en la Whitelist usando el email
            Whitelist whitelistEntry = whitelistRepository.findByCorreo(usernameEmail)
                    .orElseThrow(() -> new RuntimeException("Email no encontrado en Whitelist: " + usernameEmail));

            // Obtener el Usuario desde la Whitelist
            Usuario usuarioActual = whitelistEntry.getUsuario();

            if (usuarioActual == null) {
                throw new RuntimeException("La entrada de Whitelist no tiene un usuario asociado.");
            }

            // Crear y guardar el log
            RegistroActividad registro = new RegistroActividad();
            registro.setFechaActividad(LocalDateTime.now());
            registro.setTipoActividad(tipoActividad);
            registro.setUsuario(usuarioActual);
            registro.setCrf(crf);

            registroRepository.save(registro);

        } catch (Exception e) {
            System.err.println("Error al guardar log de actividad: " + e.getMessage());
        }
    }

    @Transactional
    public List<RegistroActividadDTO> getUltimosRegistrosDTO(int limite) {
        // Crear solicitud de paginación: Página 0, tamaño 'limite', ordenado por fecha
        // descendente
        Pageable pageable = PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "fechaActividad"));

        // Obtener las entidades de la BD
        List<RegistroActividad> entidades = registroRepository.findAll(pageable).getContent();

        // Convertir Entidades a DTOs
        return entidades.stream().map(reg -> {
            RegistroActividadDTO dto = new RegistroActividadDTO();
            dto.setIdRegistro(reg.getIdRegistro());

            if (reg.getCrf() != null) {
                dto.setCrfId(reg.getCrf().getIdCrf());
            }
            
            // Transformación de texto amigable
            String actividadAmigable = switch (reg.getTipoActividad()) {
                case "CREACION_CRF" -> "Creó una ficha";
                case "ACTUALIZACION_CRF" -> "Editó una ficha";
                case "ELIMINACION_CRF" -> "Eliminó una ficha";
                case "CREACION_USUARIO" -> "Nuevo usuario";
                default -> reg.getTipoActividad();
            };
            dto.setTipoActividad(actividadAmigable);

            dto.setFechaActividad(reg.getFechaActividad());

            // Manejo seguro de nulos para el usuario
            if (reg.getUsuario() != null) {
                dto.setUsuarioNombre(reg.getUsuario().getNombreUsuario());
            } else {
                dto.setUsuarioNombre("Usuario Eliminado");
            }

            return dto;
        }).collect(Collectors.toList());
    }
}