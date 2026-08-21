package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.dto.ProductoVarianteDTO;
import backendWebProyectoApp.WebBackendApp.entidades.ProductoVariante;
import backendWebProyectoApp.WebBackendApp.entidades.Productos;
import backendWebProyectoApp.WebBackendApp.entidades.Tamaños;
import backendWebProyectoApp.WebBackendApp.repositorios.ProductoVarianteRepository;
import backendWebProyectoApp.WebBackendApp.repositorios.ProductosRepository;
import backendWebProyectoApp.WebBackendApp.repositorios.TamanoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoVarianteService {

    @Autowired
    private ProductoVarianteRepository repo;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private TamanoRepository tamanoRepository;

    public ProductoVariante crear(ProductoVarianteDTO dto) {

        Productos producto = productosRepository.findById(dto.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no existe"));

        Tamaños tamano = tamanoRepository.findById(dto.getIdTamano())
                .orElseThrow(() -> new RuntimeException("Tamaño no existe"));

        boolean existe = repo.findByProducto_IdAndTamaño_Id(
                producto.getId(),
                tamano.getId()
        ).isPresent();

        if (existe) {
            throw new RuntimeException("Ya existe esta variante");
        }

        ProductoVariante v = new ProductoVariante();
        v.setProducto(producto);
        v.setTamaño(tamano);
        v.setPrecio(dto.getPrecio());

        return repo.save(v);
    }

    public ProductoVariante actualizarPrecioPromo(Integer id, java.math.BigDecimal precioPromo) {
        ProductoVariante v = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Variante no existe"));
        v.setPrecioPromo(precioPromo);
        return repo.save(v);
    }

    public List<ProductoVariante> porProducto(Integer idProducto) {
        return repo.findByProducto_Id(idProducto);
    }

    public List<ProductoVariante> listarTodas() {
        return repo.findAll();
    }

    public ProductoVariante actualizar(Integer id, ProductoVarianteDTO dto) {

        ProductoVariante v = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Variante no existe"));

        v.setPrecio(dto.getPrecio());

        return repo.save(v);
    }

    public void eliminar(Integer id) {
        repo.deleteById(id);
    }
}