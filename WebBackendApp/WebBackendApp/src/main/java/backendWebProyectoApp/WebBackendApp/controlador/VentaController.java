package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.dto.CrearVentaDTO;
import backendWebProyectoApp.WebBackendApp.dto.VentaDTO;
import backendWebProyectoApp.WebBackendApp.entidades.Venta;
import backendWebProyectoApp.WebBackendApp.servicios.VentaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private VentaServicio servicio;

    @Autowired
    private SimpMessagingTemplate mensajeria;

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @PostMapping("/crear")
    public ResponseEntity<VentaDTO> crear(@RequestBody CrearVentaDTO dto) {
        VentaDTO nueva = servicio.crearVenta(dto);
        mensajeria.convertAndSend("/topic/ventas", nueva);
        return ResponseEntity.ok(nueva);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/todas")
    public ResponseEntity<List<VentaDTO>> todas() {
        return ResponseEntity.ok(servicio.verVentas());
    }

    @GetMapping("/mis-ventas")
    public ResponseEntity<List<VentaDTO>> misVentas() {
        return ResponseEntity.ok(servicio.verMisVentas());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuario/{nombre}")
    public ResponseEntity<List<VentaDTO>> porUsuario(@PathVariable String nombre) {
        return ResponseEntity.ok(servicio.verPorNombreUsuario(nombre));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sede/{id}")
    public ResponseEntity<List<VentaDTO>> porSede(@PathVariable Integer id) {
        return ResponseEntity.ok(servicio.verPorSede(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/modificar/{id}")
    public ResponseEntity<VentaDTO> modificar(
            @PathVariable Integer id,
            @RequestBody CrearVentaDTO dto) {

        return ResponseEntity.ok(servicio.modificarVenta(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Integer id) {
        servicio.eliminarVenta(id);
        return ResponseEntity.ok("Venta eliminada correctamente");
    }
}