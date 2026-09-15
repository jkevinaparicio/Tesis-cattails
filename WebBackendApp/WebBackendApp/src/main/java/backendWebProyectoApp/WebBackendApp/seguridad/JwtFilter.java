package backendWebProyectoApp.WebBackendApp.seguridad;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            String correo = jwtUtil.extractCorreo(token);
            List<String> roles = jwtUtil.extractRoles(token);

            List<SimpleGrantedAuthority> authorities = roles != null
                    ? roles.stream()
                            .flatMap(r -> {
                                String rUpper = r.toUpperCase();
                                String roleName = rUpper.startsWith("ROLE_") ? rUpper : "ROLE_" + rUpper;
                                return java.util.stream.Stream.of(
                                        new SimpleGrantedAuthority(r),
                                        new SimpleGrantedAuthority(rUpper),
                                        new SimpleGrantedAuthority(roleName)
                                );
                            })
                            .distinct()
                            .toList()
                    : List.of();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            correo,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            System.err.println("Error validando JWT en JwtFilter: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

}
