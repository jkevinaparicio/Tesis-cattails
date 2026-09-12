package backendWebProyectoApp.WebBackendApp.repositorios;

import backendWebProyectoApp.WebBackendApp.entidades.Usuario;
import backendWebProyectoApp.WebBackendApp.entidades.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.time.LocalDate;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    List<Venta> findAllByOrderByFechaDesc();
    List<Venta> findByUsuario_NombreOrderByFechaDesc(String nombre);
    List<Venta> findBySede_IdOrderByFechaDesc(Integer idSede);
    List<Venta> findByUsuarioOrderByFechaDesc(Usuario usuario);
    List<Venta> findByFechaNegocioOrderByFechaDesc(LocalDate fechaNegocio);
    List<Venta> findBySede_IdAndFechaNegocioOrderByFechaDesc(Integer idSede, LocalDate fechaNegocio);

    Page<Venta> findAllByOrderByFechaDesc(Pageable pageable);
    Page<Venta> findByUsuario_NombreOrderByFechaDesc(String nombre, Pageable pageable);
    Page<Venta> findBySede_IdOrderByFechaDesc(Integer idSede, Pageable pageable);
    Page<Venta> findByFechaNegocioOrderByFechaDesc(LocalDate fechaNegocio, Pageable pageable);
    Page<Venta> findBySede_IdAndFechaNegocioOrderByFechaDesc(Integer idSede, LocalDate fechaNegocio, Pageable pageable);
}
