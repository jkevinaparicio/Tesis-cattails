package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.entidades.Sede;
import backendWebProyectoApp.WebBackendApp.servicios.SedeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sedes")
@RequiredArgsConstructor
public class SedeController {

    private final SedeService sedeService;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/listar")
    public List<Sede> listar() {
        return sedeService.listar();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/crear")
    public Sede crear(@RequestBody Sede sede) {
        return sedeService.crear(sede);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/modificar/{id}")
    public Sede actualizar(@PathVariable Integer id, @RequestBody Sede sede) {
        return sedeService.actualizar(id, sede);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public void eliminar(@PathVariable Integer id) {
        sedeService.eliminar(id);
    }
}