package backendWebProyectoApp.WebBackendApp.repositorios;

import backendWebProyectoApp.WebBackendApp.entidades.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findById(Integer id);
    Optional<Rol> findByNombre(String nombre);
}