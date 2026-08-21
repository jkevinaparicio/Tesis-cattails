package backendWebProyectoApp.WebBackendApp.repositorios;

import backendWebProyectoApp.WebBackendApp.entidades.Tamaños;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TamanoRepository extends JpaRepository<Tamaños, Integer> {

    Optional<Tamaños> findByNombre(String nombre);

    Optional<Tamaños> findById(Integer id);

}
