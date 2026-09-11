package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.dto.CrearVentaDTO;
import backendWebProyectoApp.WebBackendApp.dto.VentaDTO;
import backendWebProyectoApp.WebBackendApp.entidades.Venta;
import backendWebProyectoApp.WebBackendApp.servicios.VentaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/ventas")
@CrossOrigin("*")
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

    // Cierre de caja: todas las ventas de un día de negocio (viernes/sábado
    // incluyen lo vendido hasta la 1am del día siguiente)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<VentaDTO>> porFechaNegocio(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(servicio.verPorFechaNegocio(fecha));
    }

    // Igual que arriba pero filtrado por sede, para el cierre de cada local
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/sede/{id}/fecha/{fecha}")
    public ResponseEntity<List<VentaDTO>> porSedeYFecha(
            @PathVariable Integer id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(servicio.verPorSedeYFechaNegocio(id, fecha));
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
