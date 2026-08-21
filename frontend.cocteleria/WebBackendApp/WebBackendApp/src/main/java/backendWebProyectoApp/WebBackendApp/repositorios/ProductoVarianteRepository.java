package backendWebProyectoApp.WebBackendApp.repositorios;

import backendWebProyectoApp.WebBackendApp.entidades.ProductoVariante;
import backendWebProyectoApp.WebBackendApp.entidades.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Integer> {
    List<ProductoVariante> findByProductoId(Integer idProducto);

    Optional<ProductoVariante> findByProducto_IdAndTamaño_Id(Integer productoId, Integer tamanoId);

    List<ProductoVariante> findByProducto_Id(Integer idProducto);
    
}
