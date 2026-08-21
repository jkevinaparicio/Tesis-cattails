package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.entidades.Tamaños;
import backendWebProyectoApp.WebBackendApp.repositorios.TamanoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TamanoService {

    @Autowired
    private TamanoRepository repository;

    public Tamaños crear(Tamaños t) {
        return repository.save(t);
    }

    public List<Tamaños> listar() {
        return repository.findAll();
    }

    public Tamaños modificar(Integer id, Tamaños t) {

        Tamaños existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tamaño no existe"));

        existente.setNombre(t.getNombre());

        return repository.save(existente);
    }

    public void eliminar(Integer id) {
        Tamaños t = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tamaño no existe"));

        repository.delete(t);
    }
}
