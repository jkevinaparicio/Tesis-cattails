package backendWebProyectoApp.WebBackendApp.repositorios;

import backendWebProyectoApp.WebBackendApp.entidades.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Integer> {
    Optional<Inventario> findByVarianteIdAndSedeId(Integer varianteId, Integer sedeId);
    Optional<Inventario> findByVariante_IdAndSede_Id(Integer varianteId, Integer sedeId);

    List<Inventario> findBySede_Id(Integer sedeId);

}