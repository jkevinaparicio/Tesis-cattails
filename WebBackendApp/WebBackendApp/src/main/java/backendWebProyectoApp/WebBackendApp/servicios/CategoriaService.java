package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.entidades.Categoria;
import backendWebProyectoApp.WebBackendApp.entidades.ProductoVariante;
import backendWebProyectoApp.WebBackendApp.entidades.Productos;
import backendWebProyectoApp.WebBackendApp.repositorios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository repository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private ProductoVarianteRepository varianteRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private InventarioTamanoRepository inventarioTamanoRepository;

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
        existente.setCompartirStockPorTamano(c.isCompartirStockPorTamano());

        return repository.save(existente);
    }

    @Transactional
    public void eliminar(Integer id) {
        Categoria c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no existe"));

        // 1. Para cada producto de la categoría, borrar inventarios y variantes
        List<Productos> productos = productosRepository.findByCategoria_Id(id);
        for (Productos p : productos) {
            List<ProductoVariante> variantes = varianteRepository.findByProducto_Id(p.getId());
            for (ProductoVariante v : variantes) {
                inventarioRepository.deleteByVariante_Id(v.getId());
            }
            varianteRepository.deleteByProducto_Id(p.getId());
        }

        // 2. Borrar los productos de la categoría
        for (Productos p : productos) {
            productosRepository.delete(p);
        }

        // 3. Borrar inventarios_tamano asociados a la categoría (si hay)
        // (ya no tienen referencias de categoría directa, se limpian con los productos)

        // 4. Eliminar la categoría
        repository.delete(c);
    }
}