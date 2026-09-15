package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.entidades.Categoria;
import backendWebProyectoApp.WebBackendApp.entidades.ProductoVariante;
import backendWebProyectoApp.WebBackendApp.entidades.Productos;
import backendWebProyectoApp.WebBackendApp.repositorios.CategoriaRepository;
import backendWebProyectoApp.WebBackendApp.repositorios.InventarioRepository;
import backendWebProyectoApp.WebBackendApp.repositorios.ProductoVarianteRepository;
import backendWebProyectoApp.WebBackendApp.repositorios.ProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductosRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoVarianteRepository varianteRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    public Productos crear(Productos p) {

        Categoria categoria = categoriaRepository.findById(p.getCategoria().getId())
                .orElseThrow(() -> new RuntimeException("Categoria no existe"));

        p.setCategoria(categoria);

        return productoRepository.save(p);
    }

    public List<Productos> listar() {
        return productoRepository.findAll();
    }

    public Productos modificar(Integer id, Productos p) {

        Productos existente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no existe"));

        Categoria categoria = categoriaRepository.findById(p.getCategoria().getId())
                .orElseThrow(() -> new RuntimeException("Categoria no existe"));

        existente.setNombre(p.getNombre());
        existente.setCategoria(categoria);

        return productoRepository.save(existente);
    }

    @Transactional
    public void eliminar(Integer id) {
        Productos p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no existe"));

        // 1. Borrar inventarios de cada variante del producto
        List<ProductoVariante> variantes = varianteRepository.findByProducto_Id(id);
        for (ProductoVariante v : variantes) {
            inventarioRepository.deleteByVariante_Id(v.getId());
        }

        // 2. Borrar las variantes del producto
        varianteRepository.deleteByProducto_Id(id);

        // 3. Eliminar el producto
        productoRepository.delete(p);
    }
}
