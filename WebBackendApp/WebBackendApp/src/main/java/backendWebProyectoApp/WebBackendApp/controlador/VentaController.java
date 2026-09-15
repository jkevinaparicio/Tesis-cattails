package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.dto.CrearVentaDTO;
import backendWebProyectoApp.WebBackendApp.dto.VentaDTO;
import backendWebProyectoApp.WebBackendApp.entidades.Venta;
import backendWebProyectoApp.WebBackendApp.servicios.VentaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<Page<VentaDTO>> todas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(servicio.verVentasPaginado(page, size));
    }

    @GetMapping("/mis-ventas")
    public ResponseEntity<List<VentaDTO>> misVentas() {
        return ResponseEntity.ok(servicio.verMisVentas());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuario/{nombre}")
    public ResponseEntity<Page<VentaDTO>> porUsuario(
            @PathVariable String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(servicio.verPorNombreUsuarioPaginado(nombre, page, size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sede/{id}")
    public ResponseEntity<Page<VentaDTO>> porSede(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(servicio.verPorSedePaginado(id, page, size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<Page<VentaDTO>> porFechaNegocio(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(servicio.verPorFechaNegocioPaginado(fecha, page, size));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @GetMapping("/sede/{id}/fecha/{fecha}")
    public ResponseEntity<Page<VentaDTO>> porSedeYFecha(
            @PathVariable Integer id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(servicio.verPorSedeYFechaNegocioPaginado(id, fecha, page, size));
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
