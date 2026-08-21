package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.entidades.Categoria;
import backendWebProyectoApp.WebBackendApp.servicios.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categoria")
@CrossOrigin("*")
public class CategoriaController {

    @Autowired
    private CategoriaService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/crear")
    public Categoria crear(@RequestBody Categoria c) {
        return service.crear(c);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/todas")
    public List<Categoria> listar() {
        return service.listar();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/modificar/{id}")
    public Categoria modificar(@PathVariable Integer id, @RequestBody Categoria c) {
        return service.modificar(id, c);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return "Categoria eliminada";
    }
}
