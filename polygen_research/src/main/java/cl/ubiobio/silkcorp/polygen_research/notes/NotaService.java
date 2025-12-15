package cl.ubiobio.silkcorp.polygen_research.notes;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class NotaService {

    private final NotaRepository notaRepository;

    public NotaService(NotaRepository notaRepository) {
        this.notaRepository = notaRepository;
    }

    public List<Nota> listarTodas() {
        return notaRepository.findAll();
    }

    public void guardar(Nota nota) {
        notaRepository.save(nota);
    }

    public void eliminar(Long id) {
        notaRepository.deleteById(id);
    }
}