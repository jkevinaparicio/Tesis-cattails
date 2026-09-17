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
import java.time.LocalDateTime;

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
        String correoGoogle = oauth2User.getAttribute("email");

        if (correoGoogle == null || correoGoogle.isBlank()) {
            response.sendRedirect(frontendUrl + "/login?error=sin_correo");
            return;
        }

        if (!correoVerificadoPorGoogle(oauth2User)) {
            response.sendRedirect(frontendUrl + "/login?error=correo_no_verificado");
            return;
        }

        Usuario usuario = usuarioRepository.findByCorreo(correoGoogle)
                .orElse(null);

        if (usuario == null) {
            response.sendRedirect(frontendUrl + "/login?error=no_registrado");
            return;
        }

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            response.sendRedirect(frontendUrl + "/login?error=inactivo");
            return;
        }

        usuario.setUltimaConexion(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String rol   = usuario.getRol().getNombre();
        String token = jwtUtil.generateToken(correoGoogle, rol);

        response.sendRedirect(frontendUrl + "/oauth2/callback?token=" + token + "&rol=" + rol);
    }

    private boolean correoVerificadoPorGoogle(OAuth2User oauth2User) {
        Object valor = oauth2User.getAttribute("email_verified");

        if (valor instanceof Boolean b) {
            return b;
        }
        if (valor instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return false;
    }
}
