package backendWebProyectoApp.WebBackendApp.repositorios;

import backendWebProyectoApp.WebBackendApp.entidades.InventarioTamano;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioTamanoRepository extends JpaRepository<InventarioTamano, Integer> {
    Optional<InventarioTamano> findByTamano_IdAndSede_Id(Integer tamanoId, Integer sedeId);

    List<InventarioTamano> findBySede_Id(Integer sedeId);

    void deleteByTamano_IdAndSede_Id(Integer tamanoId, Integer sedeId);

    void deleteByTamano_Id(Integer tamanoId);
}
