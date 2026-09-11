package backendWebProyectoApp.WebBackendApp.repositorios;

import backendWebProyectoApp.WebBackendApp.entidades.Usuario;
import backendWebProyectoApp.WebBackendApp.entidades.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDate;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    List<Venta> findByUsuarioOrderByFechaDesc(Usuario usuario);

    List<Venta> findByUsuario_NombreOrderByFechaDesc(String nombre);

    List<Venta> findBySede_IdOrderByFechaDesc(Integer idSede);

    List<Venta> findAllByOrderByFechaDesc();

    List<Venta> findByFechaNegocioOrderByFechaDesc(LocalDate fechaNegocio);

    List<Venta> findBySede_IdAndFechaNegocioOrderByFechaDesc(Integer idSede, LocalDate fechaNegocio);




}
