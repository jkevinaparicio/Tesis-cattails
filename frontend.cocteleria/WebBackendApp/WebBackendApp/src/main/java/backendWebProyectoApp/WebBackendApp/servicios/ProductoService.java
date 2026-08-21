package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.entidades.Categoria;
import backendWebProyectoApp.WebBackendApp.entidades.Productos;
import backendWebProyectoApp.WebBackendApp.repositorios.CategoriaRepository;
import backendWebProyectoApp.WebBackendApp.repositorios.ProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductosRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

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

    public void eliminar(Integer id) {
        Productos p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no existe"));

        productoRepository.delete(p);
    }
}
