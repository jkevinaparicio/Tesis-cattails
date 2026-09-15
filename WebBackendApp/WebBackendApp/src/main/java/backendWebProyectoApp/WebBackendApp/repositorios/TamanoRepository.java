package backendWebProyectoApp.WebBackendApp.repositorios;

import backendWebProyectoApp.WebBackendApp.entidades.Tamano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TamanoRepository extends JpaRepository<Tamano, Integer> {

    Optional<Tamano> findByNombre(String nombre);

    Optional<Tamano> findById(Integer id);

}
