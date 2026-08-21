package backendWebProyectoApp.WebBackendApp.seguridad;

import backendWebProyectoApp.WebBackendApp.entidades.Usuario;
import backendWebProyectoApp.WebBackendApp.repositorios.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String correoGoogle = oauth2User.getAttribute("email"); // correo real de Google

        // 1. ¿Existe en la BD?
        Usuario usuario = usuarioRepository.findByCorreo(correoGoogle)
                .orElse(null);

        if (usuario == null) {
            // No registrado por el admin → rechazar
            response.sendRedirect(frontendUrl + "/login?error=no_registrado");
            return;
        }

        // 2. ¿Está activo?
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            response.sendRedirect(frontendUrl + "/login?error=inactivo");
            return;
        }

        // 3. Todo bien → generar JWT propio (igual que el login clásico)
        String rol   = usuario.getRol().getNombre();
        String token = jwtUtil.generateToken(correoGoogle, rol);

        // 4. Redirigir al frontend con el token en la URL
        //    El frontend lo captura y lo guarda en localStorage
        response.sendRedirect(frontendUrl + "/oauth2/callback?token=" + token + "&rol=" + rol);
    }
}