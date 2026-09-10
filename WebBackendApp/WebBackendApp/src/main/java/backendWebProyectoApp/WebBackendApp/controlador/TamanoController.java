package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.entidades.Tamaños;
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
    public Tamaños crear(@RequestBody Tamaños t) {
        return service.crear(t);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/todos")
    public List<Tamaños> listar() {
        return service.listar();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/modificar/{id}")
    public Tamaños modificar(@PathVariable Integer id, @RequestBody Tamaños t) {
        return service.modificar(id, t);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return "Tamaño eliminado";
    }
}
