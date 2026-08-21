package backendWebProyectoApp.WebBackendApp.repositorios;

import backendWebProyectoApp.WebBackendApp.entidades.DetalleVenta;
import backendWebProyectoApp.WebBackendApp.entidades.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta,Integer> {
    List<DetalleVenta> findByVenta(Venta venta);

    // Ventas diarias (para calcular tendencia)
    @Query("""
    SELECT FUNCTION('DATE', v.fecha), SUM(v.total)
    FROM Venta v
    WHERE v.fecha IS NOT NULL
    GROUP BY FUNCTION('DATE', v.fecha)
    ORDER BY FUNCTION('DATE', v.fecha)
    """)
    List<Object[]> ventasPorDia();

    // Ventas mensuales por empleado (para predecir rendimiento)
    @Query("""
    SELECT u.nombre, u.apellido, FUNCTION('DATE_FORMAT', v.fecha, '%Y-%m'), SUM(v.total), COUNT(v)
    FROM Venta v
    JOIN v.usuario u
    WHERE v.fecha IS NOT NULL
    GROUP BY u.nombre, u.apellido, FUNCTION('DATE_FORMAT', v.fecha, '%Y-%m')
    ORDER BY u.nombre, FUNCTION('DATE_FORMAT', v.fecha, '%Y-%m')
    """)
    List<Object[]> ventasMensualesPorEmpleado();

    // Top productos más vendidos por cantidad
    @Query("""
        SELECT d.producto.nombre, SUM(d.cantidad) as total
        FROM DetalleVenta d
        GROUP BY d.producto.nombre
        ORDER BY total DESC
    """)
    List<Object[]> topProductosMasVendidos();

    // Ventas por sede
    @Query("""
        SELECT v.sede.nombre, COUNT(v), SUM(v.total)
        FROM Venta v
        GROUP BY v.sede.nombre
    """)
    List<Object[]> ventasPorSede();

    // Ventas por empleado
    @Query("""
        SELECT v.usuario.nombre, v.usuario.apellido, COUNT(v), SUM(v.total)
        FROM Venta v
        GROUP BY v.usuario.nombre, v.usuario.apellido
        ORDER BY SUM(v.total) DESC
    """)
    List<Object[]> ventasPorEmpleado();

    // Productos por categoría
    @Query("""
        SELECT d.producto.categoria.nombre, SUM(d.cantidad)
        FROM DetalleVenta d
        GROUP BY d.producto.categoria.nombre
        ORDER BY SUM(d.cantidad) DESC
    """)
    List<Object[]> ventasPorCategoria();

    // Ventas por método de pago
    @Query("""
        SELECT v.metodoPago, COUNT(v), SUM(v.total)
        FROM Venta v
        GROUP BY v.metodoPago
    """)
    List<Object[]> ventasPorMetodoPago();
}
