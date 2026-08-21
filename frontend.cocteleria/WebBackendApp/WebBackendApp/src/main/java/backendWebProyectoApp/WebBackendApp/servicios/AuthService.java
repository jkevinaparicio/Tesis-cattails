package backendWebProyectoApp.WebBackendApp.servicios;


import backendWebProyectoApp.WebBackendApp.dto.AuthRequest;
import backendWebProyectoApp.WebBackendApp.dto.AuthResponse;
import backendWebProyectoApp.WebBackendApp.entidades.Rol;
import backendWebProyectoApp.WebBackendApp.entidades.Usuario;
import backendWebProyectoApp.WebBackendApp.repositorios.RolRepository;
import backendWebProyectoApp.WebBackendApp.repositorios.UsuarioRepository;
import backendWebProyectoApp.WebBackendApp.seguridad.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final PasswordEncoder encoder;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final JwtUtil jwtUtil;

    public AuthService(PasswordEncoder encoder,
                       UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       JwtUtil jwtUtil) {
        this.encoder = encoder;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(Usuario usuario) {

        usuario.setContraseña(encoder.encode(usuario.getContraseña()));

        Rol rol = rolRepository.findById(usuario.getRol().getId())
                .orElseThrow(() -> new RuntimeException("Rol no existe"));


        usuario.setRol(rol);



        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(
                usuario.getCorreo(),
                rol.getNombre()
        );

        return new AuthResponse(token, rol.getNombre());

    }

    public AuthResponse login(AuthRequest request) {

        Usuario user = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no existe"));

        if (!encoder.matches(request.getContraseña(), user.getContraseña())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        if (Boolean.FALSE.equals(user.getActivo())) {
            throw new RuntimeException("Usuario inactivo");
        }

        user.setUltimaConexion(LocalDateTime.now());
        usuarioRepository.save(user);

        String roles = user.getRol().getNombre();
        String token = jwtUtil.generateToken(user.getCorreo(), roles);

        return new AuthResponse(token, roles);
    }
}
