package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.servicios.AsistenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/asistente")
@RequiredArgsConstructor
public class AsistenteController {

    private final AsistenteService asistenteService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/preguntar")
    public Map<String, String> preguntar(@RequestBody Map<String, String> body) {
        String pregunta  = body.get("pregunta");
        String respuesta = asistenteService.preguntar(pregunta);
        return Map.of("respuesta", respuesta);
    }
}