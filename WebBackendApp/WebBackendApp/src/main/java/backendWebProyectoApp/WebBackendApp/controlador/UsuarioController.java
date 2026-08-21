package backendWebProyectoApp.WebBackendApp.controlador;

import backendWebProyectoApp.WebBackendApp.entidades.Usuario;
import backendWebProyectoApp.WebBackendApp.servicios.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import backendWebProyectoApp.WebBackendApp.config.PresenciaWebSocket;
import java.util.Set;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PresenciaWebSocket presencia;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/crear")
    public String crear(@RequestBody Usuario usuario) {
        usuarioService.crear(usuario);
        return "creado";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/conectados")
    public Set<String> conectados() {
        return presencia.getCorreosConectados();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/modificar/{id}")
    public Usuario actualizar(@PathVariable Integer id, @RequestBody Usuario usuario) {
        return usuarioService.actualizar(id, usuario);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public void eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
    }
}
