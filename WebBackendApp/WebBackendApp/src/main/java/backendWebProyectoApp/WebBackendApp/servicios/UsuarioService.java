package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.entidades.Usuario;
import backendWebProyectoApp.WebBackendApp.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario crear(Usuario usuario) {
        usuario.setContraseña(passwordEncoder.encode(usuario.getContraseña()));
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Integer id, Usuario usuario) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        existente.setNombre(usuario.getNombre());
        existente.setApellido(usuario.getApellido());

        if (usuario.getCorreo() != null && !usuario.getCorreo().isBlank()) {
            existente.setCorreo(usuario.getCorreo());
        }

        if (usuario.getContraseña() != null && !usuario.getContraseña().isBlank()) {
            existente.setContraseña(passwordEncoder.encode(usuario.getContraseña()));
        }

        if (usuario.getRol() != null) {
            existente.setRol(usuario.getRol());
        }

        if (usuario.getActivo() != null) {
            existente.setActivo(usuario.getActivo());
        }

        return usuarioRepository.save(existente);
    }

    public void eliminar(Integer id) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        existente.setActivo(false);
        usuarioRepository.save(existente);
    }
}