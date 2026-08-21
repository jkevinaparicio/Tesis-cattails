package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.entidades.Productos;
import backendWebProyectoApp.WebBackendApp.servicios.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
@CrossOrigin("*")
public class ProductoController {

    @Autowired
    private ProductoService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/crear")
    public Productos crear(@RequestBody Productos p) {
        return service.crear(p);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/todos")
    public List<Productos> listar() {
        return service.listar();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/modificar/{id}")
    public Productos modificar(@PathVariable Integer id, @RequestBody Productos p) {
        return service.modificar(id, p);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return "Producto eliminado";
    }
}