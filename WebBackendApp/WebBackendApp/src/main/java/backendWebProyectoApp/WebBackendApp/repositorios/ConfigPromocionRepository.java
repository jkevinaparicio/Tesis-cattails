// ConfigPromocionRepository.java
package backendWebProyectoApp.WebBackendApp.repositorios;

import backendWebProyectoApp.WebBackendApp.entidades.ConfigPromocion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigPromocionRepository extends JpaRepository<ConfigPromocion, Integer> {
    java.util.Optional<ConfigPromocion> findFirstBy();
}