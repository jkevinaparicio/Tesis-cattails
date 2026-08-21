package backendWebProyectoApp.WebBackendApp.servicios;


import backendWebProyectoApp.WebBackendApp.entidades.Usuario;
import backendWebProyectoApp.WebBackendApp.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new DisabledException("Usuario inactivo");
        }

        return new org.springframework.security.core.userdetails.User(
                usuario.getCorreo(),
                usuario.getContraseña(),
                List.of(new SimpleGrantedAuthority(usuario.getRol().getNombre()))
        );
    }
}