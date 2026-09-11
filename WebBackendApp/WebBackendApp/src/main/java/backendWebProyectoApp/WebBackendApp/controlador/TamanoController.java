package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.entidades.Tamano;
import backendWebProyectoApp.WebBackendApp.servicios.TamanoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tamanos")
public class TamanoController {

    @Autowired
    private TamanoService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/crear")
    public Tamano crear(@RequestBody Tamano t) {
        return service.crear(t);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/todos")
    public List<Tamano> listar() {
        return service.listar();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/modificar/{id}")
    public Tamano modificar(@PathVariable Integer id, @RequestBody Tamano t) {
        return service.modificar(id, t);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return "Tamaño eliminado";
    }
}
