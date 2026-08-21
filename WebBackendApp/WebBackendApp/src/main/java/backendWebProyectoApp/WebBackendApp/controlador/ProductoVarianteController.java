package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.dto.ProductoVarianteDTO;
import backendWebProyectoApp.WebBackendApp.entidades.ProductoVariante;
import backendWebProyectoApp.WebBackendApp.servicios.ProductoVarianteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/variantes")
@CrossOrigin(origins = "*")
public class ProductoVarianteController {

    @Autowired
    private ProductoVarianteService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ProductoVariante crear(@RequestBody ProductoVarianteDTO dto) {
        return service.crear(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ProductoVariante> listarTodas() {
        return service.listarTodas();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/producto/{idProducto}")
    public List<ProductoVariante> porProducto(@PathVariable Integer idProducto) {
        return service.porProducto(idProducto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ProductoVariante actualizar(
            @PathVariable Integer id,
            @RequestBody ProductoVarianteDTO dto) {

        return service.actualizar(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return "Variante eliminada";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/precio-promo")
    public ResponseEntity<?> actualizarPrecioPromo(
            @PathVariable Integer id,
            @RequestBody java.util.Map<String, Object> body) {

        Object val = body.get("precioPromo");
        java.math.BigDecimal precio = (val != null && !val.toString().equals("null"))
                ? new java.math.BigDecimal(val.toString()) : null;

        return ResponseEntity.ok(service.actualizarPrecioPromo(id, precio));
    }

}