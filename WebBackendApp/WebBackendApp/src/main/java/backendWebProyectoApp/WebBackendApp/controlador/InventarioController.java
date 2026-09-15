package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.dto.InventarioDTO;
import backendWebProyectoApp.WebBackendApp.dto.InventarioRequestDTO;
import backendWebProyectoApp.WebBackendApp.dto.MovimientoStockDTO;
import backendWebProyectoApp.WebBackendApp.entidades.Inventario;
import backendWebProyectoApp.WebBackendApp.entidades.InventarioTamano;
import backendWebProyectoApp.WebBackendApp.servicios.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    private InventarioService service;

    // 🟢 AUMENTAR STOCK (POR VARIANTE O DINÁMICO)
    @PostMapping("/aumentar")
    public void aumentarStock(@RequestBody MovimientoStockDTO dto) {
        service.aumentarStock(dto.getVarianteId(), dto.getSedeId(), dto.getCantidad());
    }

    // 🔴 DISMINUIR STOCK (POR VARIANTE O DINÁMICO)
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

    // ==========================================
    // 🍹 INVENTARIO COMPARTIDO POR TAMAÑO
    // ==========================================

    @GetMapping("/tamano/sede/{idSede}")
    public List<InventarioTamano> listarTamanoPorSede(@PathVariable Integer idSede) {
        return service.listarTamanoPorSede(idSede);
    }

    @PostMapping("/tamano/crear")
    public InventarioTamano crearTamano(@RequestBody InventarioDTO dto) {
        return service.crearOActualizarTamano(dto.getIdTamano(), dto.getIdSede(), dto.getStock());
    }

    @PostMapping("/tamano/aumentar")
    public void aumentarStockTamano(@RequestBody MovimientoStockDTO dto) {
        service.aumentarStockTamano(dto.getTamanoId(), dto.getSedeId(), dto.getCantidad());
    }

    @PostMapping("/tamano/disminuir")
    public void disminuirStockTamano(@RequestBody MovimientoStockDTO dto) {
        service.descontarStockTamano(dto.getTamanoId(), dto.getSedeId(), dto.getCantidad());
    }

    @DeleteMapping("/tamano/sede/{idSede}/tamano/{idTamano}")
    public String eliminarTamanoDeSede(
            @PathVariable Integer idSede,
            @PathVariable Integer idTamano) {

        service.eliminarTamanoDeSede(idTamano, idSede);
        return "Inventario de tamaño eliminado de la sede";
    }

    @GetMapping("/tamano/stock")
    public InventarioTamano obtenerStockTamano(
            @RequestParam Integer tamanoId,
            @RequestParam Integer sedeId) {
        return service.obtenerStockTamano(tamanoId, sedeId);
    }
}