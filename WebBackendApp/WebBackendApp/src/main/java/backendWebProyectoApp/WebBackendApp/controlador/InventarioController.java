package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.dto.InventarioRequestDTO;
import backendWebProyectoApp.WebBackendApp.dto.MovimientoStockDTO;
import backendWebProyectoApp.WebBackendApp.entidades.Inventario;
import backendWebProyectoApp.WebBackendApp.servicios.InventarioService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventario")
@CrossOrigin(origins = "*")
public class InventarioController {

    @Autowired
    private InventarioService service;

    // 🟢 AUMENTAR STOCK
    @PostMapping("/aumentar")
    public void aumentarStock(@RequestBody MovimientoStockDTO dto) {
        service.aumentarStock(dto.getVarianteId(), dto.getSedeId(), dto.getCantidad());
    }

    // 🔴 DISMINUIR STOCK
    @PostMapping("/disminuir")
    public void disminuirStock(@RequestBody MovimientoStockDTO dto) {
        service.descontarStock(dto.getVarianteId(), dto.getSedeId(), dto.getCantidad());
    }

    @DeleteMapping("/sede/{idSede}/variante/{idVariante}")
    public String eliminarDeSede(
            @PathVariable Integer idSede,
            @PathVariable Integer idVariante) {

        service.eliminarDeSede(idVariante, idSede);
        return "Producto eliminado del inventario de la sede";
    }

    // 📊 LISTAR INVENTARIO POR SEDE
    @GetMapping("/sede/{idSede}")
    public List<Inventario> listarPorSede(@PathVariable Integer idSede) {
        return service.listarPorSede(idSede);
    }

    @PostMapping("/crear")
    public Inventario crear(@RequestBody InventarioRequestDTO dto) {
        return service.crear(dto.getIdVariante(), dto.getIdSede(), dto.getStock());
    }

    // 📦 CONSULTAR STOCK ESPECÍFICO
    @GetMapping("/stock")
    public Inventario obtenerStock(@RequestParam Integer varianteId,
                                   @RequestParam Integer sedeId) {
        return service.obtenerStock(varianteId, sedeId);
    }
}