package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.entidades.ProductoVariante;
import backendWebProyectoApp.WebBackendApp.entidades.Tamano;
import backendWebProyectoApp.WebBackendApp.repositorios.InventarioRepository;
import backendWebProyectoApp.WebBackendApp.repositorios.InventarioTamanoRepository;
import backendWebProyectoApp.WebBackendApp.repositorios.ProductoVarianteRepository;
import backendWebProyectoApp.WebBackendApp.repositorios.TamanoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TamanoService {

    @Autowired
    private TamanoRepository repository;

    @Autowired
    private ProductoVarianteRepository varianteRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private InventarioTamanoRepository inventarioTamanoRepository;

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

    @Transactional
    public void eliminar(Integer id) {
        Tamano t = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tamaño no existe"));

        // 1. Borrar inventarios (por variante) de todas las variantes del tamaño
        List<ProductoVariante> variantes = varianteRepository.findByTamaño_Id(id);
        for (ProductoVariante v : variantes) {
            inventarioRepository.deleteByVariante_Id(v.getId());
        }

        // 2. Borrar inventarios_tamano del tamaño (pool compartido)
        inventarioTamanoRepository.deleteByTamano_Id(id);

        // 3. Borrar variantes del tamaño
        varianteRepository.deleteByTamaño_Id(id);

        // 4. Eliminar el tamaño
        repository.delete(t);
    }
}
