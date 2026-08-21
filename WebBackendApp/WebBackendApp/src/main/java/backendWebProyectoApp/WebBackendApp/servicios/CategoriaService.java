package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.entidades.Categoria;
import backendWebProyectoApp.WebBackendApp.repositorios.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository repository;

    public Categoria crear(Categoria c) {
        return repository.save(c);
    }

    public List<Categoria> listar() {
        return repository.findAll();
    }

    public Categoria modificar(Integer id, Categoria c) {
        Categoria existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no existe"));

        existente.setNombre(c.getNombre());

        return repository.save(existente);
    }

    public void eliminar(Integer id) {
        Categoria c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no existe"));

        repository.delete(c);
    }
}