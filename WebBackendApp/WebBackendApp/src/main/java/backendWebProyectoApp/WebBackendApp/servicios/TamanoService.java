package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.entidades.Tamano;
import backendWebProyectoApp.WebBackendApp.repositorios.TamanoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TamanoService {

    @Autowired
    private TamanoRepository repository;

    public Tamano crear(Tamano t) {
        return repository.save(t);
    }

    public List<Tamano> listar() {
        return repository.findAll();
    }

    public Tamano modificar(Integer id, Tamano t) {

        Tamano existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tamaño no existe"));

        existente.setNombre(t.getNombre());

        return repository.save(existente);
    }

    public void eliminar(Integer id) {
        Tamano t = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tamaño no existe"));

        repository.delete(t);
    }
}
