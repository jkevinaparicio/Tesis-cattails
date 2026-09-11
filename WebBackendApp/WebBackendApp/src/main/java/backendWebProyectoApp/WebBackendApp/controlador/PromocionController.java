package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.entidades.ConfigPromocion;
import backendWebProyectoApp.WebBackendApp.repositorios.ConfigPromocionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/promociones")
public class PromocionController {

    @Autowired
    private ConfigPromocionRepository repo;

    @GetMapping("/config")
    public ResponseEntity<?> obtener() {
        ConfigPromocion c = repo.findFirstBy().orElseGet(ConfigPromocion::new);
        return ResponseEntity.ok(c);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/config")
    public ResponseEntity<?> actualizar(@RequestBody ConfigPromocion body) {
        ConfigPromocion c = repo.findFirstBy().orElseGet(ConfigPromocion::new);
        c.setLunes(body.getLunes());
        c.setMartes(body.getMartes());
        c.setMiercoles(body.getMiercoles());
        c.setJueves(body.getJueves());
        c.setViernes(body.getViernes());
        c.setSabado(body.getSabado());
        c.setDomingo(body.getDomingo());
        return ResponseEntity.ok(repo.save(c));
    }
}